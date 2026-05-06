import React, { useState, useEffect } from 'react';
import api from '../api/api';
import ProductCard from '../components/ProductCard';

function Catalog() {
    const [externalProducts, setExternalProducts] = useState([]);
    const [loadingExternal, setLoadingExternal] = useState(false);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [vehicles, setVehicles] = useState([]);
    const [selectedVehicle, setSelectedVehicle] = useState('');

    // Пагинация
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalItems, setTotalItems] = useState(0);
    const pageSize = 12;

    useEffect(() => {
        loadProducts();
        loadVehicles();
    }, [currentPage]);

    const loadProducts = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/products/page?page=${currentPage}&size=${pageSize}`);
            if (response.data.success) {
                const data = response.data.data;
                setProducts(data.products);
                setTotalPages(data.totalPages);
                setTotalItems(data.totalItems);
            }
        } catch (error) {
            console.error('Ошибка загрузки товаров:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadVehicles = async () => {
        try {
            const response = await api.get('/vehicles');
            if (response.data.success) {
                setVehicles(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки автомобилей:', error);
        }
    };

    const searchExternal = async (query) => {
        if (!query || query.trim() === '') {
            setExternalProducts([]);
            return;
        }
        setLoadingExternal(true);
        try {
            const response = await api.get(`/search/external?query=${encodeURIComponent(query)}`);
            if (response.data.success) {
                setExternalProducts(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка поиска у поставщиков:', error);
        } finally {
            setLoadingExternal(false);
        }
    };

    const handleSearch = async () => {
        setLoading(true);
        try {
            let url = '/search';
            const params = [];
            if (searchQuery) params.push(`query=${encodeURIComponent(searchQuery)}`);
            if (selectedVehicle) params.push(`vehicleId=${selectedVehicle}`);
            if (params.length > 0) url += '?' + params.join('&');

            const response = await api.get(url);
            if (response.data.success) {
                setProducts(response.data.data.products || []);
                setTotalPages(1);
                setTotalItems(products.length);
            }
        } catch (error) {
            console.error('Ошибка поиска:', error);
        } finally {
            setLoading(false);
        }

        // Поиск у поставщиков
        searchExternal(searchQuery);
    };

    const addToCart = async (product) => {
        try {
            const response = await api.post('/cart/add', {
                productId: product.id,
                quantity: 1
            });
            if (response.data.success) {
                alert(`${product.name} добавлен в корзину`);
            }
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Ошибка добавления в корзину');
        }
    };

    const requestExternalProduct = (product) => {
        alert(`Запрос на товар "${product.name}" отправлен менеджеру. Мы свяжемся с вами.`);
    };

    const goToPage = (page) => {
        if (page >= 0 && page < totalPages) {
            setCurrentPage(page);
        }
    };

    return (
        <div className="p-5">
            <h1 className="text-3xl font-bold mb-6">Каталог автозапчастей</h1>

            <div className="flex gap-3 mb-5 p-4 bg-gray-100 rounded-lg">
                <input
                    type="text"
                    placeholder="Поиск по названию или артикулу..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="flex-2 p-3 border border-gray-300 rounded-lg focus:outline-none focus:border-orange-500"
                />
                <select
                    value={selectedVehicle}
                    onChange={(e) => setSelectedVehicle(e.target.value)}
                    className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:border-orange-500"
                >
                    <option value="">Все автомобили</option>
                    {vehicles.map(vehicle => (
                        <option key={vehicle.id} value={vehicle.id}>
                            {vehicle.make} {vehicle.model} {vehicle.generation ? `(${vehicle.generation})` : ''}
                        </option>
                    ))}
                </select>
                <button
                    onClick={handleSearch}
                    className="px-6 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition cursor-pointer"
                >
                    Найти
                </button>
            </div>

            {/* Свои товары */}
            {loading ? (
                <p>Загрузка...</p>
            ) : products.length === 0 ? (
                <p className="text-center text-gray-500 py-10">Товары не найдены</p>
            ) : (
                <>
                    <div>
                        {products.map(product => (
                            <ProductCard
                                key={product.id}
                                product={product}
                                onAddToCart={addToCart}
                            />
                        ))}
                    </div>

                    {/* Пагинация */}
                    {totalPages > 1 && (
                        <div className="flex justify-center gap-3 mt-8 pt-5">
                            <button
                                onClick={() => goToPage(currentPage - 1)}
                                disabled={currentPage === 0}
                                className="px-4 py-2 bg-orange-500 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-orange-600 transition"
                            >
                                ← Назад
                            </button>
                            <span className="px-4 py-2">
                                Страница {currentPage + 1} из {totalPages}
                            </span>
                            <button
                                onClick={() => goToPage(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                                className="px-4 py-2 bg-orange-500 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-orange-600 transition"
                            >
                                Вперед →
                            </button>
                        </div>
                    )}
                </>
            )}

            {/* Товары поставщиков */}
            {(externalProducts.length > 0 || loadingExternal) && (
                <div className="mt-8 border-t pt-6">
                    <h2 className="text-xl font-bold mb-4 text-blue-600">Товары от поставщиков (под заказ)</h2>
                    <div className="bg-blue-50 p-4 rounded-lg">
                        <p className="text-sm text-gray-600 mb-3">
                            Эти товары поставляются от наших партнеров. Срок доставки 3-7 дней.
                        </p>
                        {loadingExternal ? (
                            <p>Загрузка...</p>
                        ) : (
                            <div className="space-y-3">
                                {externalProducts.map(product => (
                                    <div key={product.id} className="bg-white p-4 rounded-lg shadow">
                                        <div className="flex justify-between items-start">
                                            <div className="flex-1">
                                                <p className="font-bold text-lg">{product.name}</p>
                                                <p className="text-sm text-gray-500 mt-1">
                                                    Производитель: {product.producer} | Артикул: {product.factoryNumber}
                                                </p>
                                                <p className="text-xs text-gray-400 mt-1">Поставщик: {product.supplierName}</p>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-xl font-bold text-orange-500">{product.price} ₽</p>
                                                <p className="text-xs text-gray-500">Срок: {product.delivery}</p>
                                                <button
                                                    onClick={() => requestExternalProduct(product)}
                                                    className="mt-2 bg-orange-500 text-white px-4 py-1.5 rounded-lg text-sm hover:bg-orange-600 transition"
                                                >
                                                    Запросить
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

export default Catalog;
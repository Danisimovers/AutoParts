import React, { useState, useEffect } from 'react';
import api from '../api/api';
import ProductCard from '../components/ProductCard';

function Catalog() {
    const [externalProducts, setExternalProducts] = useState([]);
    const [loadingExternal, setLoadingExternal] = useState(false);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchType, setSearchType] = useState('contains'); // contains, startsWith, exact, name
    const [vehicles, setVehicles] = useState([]);
    const [selectedVehicle, setSelectedVehicle] = useState('');

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

            // Поиск по артикулу/названию с учетом типа поиска
            if (searchQuery) {
                let query = searchQuery;
                if (searchType === 'startsWith') {
                    query = `^${searchQuery}`;
                } else if (searchType === 'exact') {
                    query = `=${searchQuery}`;
                }
                params.push(`query=${encodeURIComponent(query)}`);
                params.push(`searchType=${searchType}`);
            }

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

    const addExternalToCart = async (product) => {
        try {
            const response = await api.post('/cart/add-external', {
                productName: product.name,
                factoryNumber: product.factoryNumber,
                producer: product.producer,
                supplierName: product.supplierName,
                price: product.price,
                quantity: 1
            });
            if (response.data.success) {
                alert(`${product.name} добавлен в корзину (товар поставщика)`);
            }
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Ошибка добавления в корзину');
        }
    };

    const goToPage = (page) => {
        if (page >= 0 && page < totalPages) {
            setCurrentPage(page);
        }
    };

    const getSearchTypeLabel = () => {
        switch(searchType) {
            case 'contains': return 'Похожие номера';
            case 'startsWith': return 'Начинается с номера';
            case 'exact': return 'Точный номер';
            case 'name': return 'По названию';
            default: return 'Поиск';
        }
    };

    const getPlaceholder = () => {
        switch(searchType) {
            case 'contains': return 'Например: 0357274 или масло';
            case 'startsWith': return 'Начинается с... Например: 0357';
            case 'exact': return 'Точный артикул... Например: D-OIL-001';
            case 'name': return 'Название товара...';
            default: return 'Поиск...';
        }
    };

    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-8 text-center">Каталог автозапчастей</h1>

            {/* Поисковая строка с типами поиска */}
            <div className="mb-8">
                <div className="flex flex-wrap gap-2 mb-3">
                    <button
                        onClick={() => setSearchType('contains')}
                        className={`px-4 py-2 rounded-lg transition ${searchType === 'contains' ? 'bg-orange-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
                    >
                        Похожие номера
                    </button>
                    <button
                        onClick={() => setSearchType('startsWith')}
                        className={`px-4 py-2 rounded-lg transition ${searchType === 'startsWith' ? 'bg-orange-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
                    >
                        Начинается с номера
                    </button>
                    <button
                        onClick={() => setSearchType('exact')}
                        className={`px-4 py-2 rounded-lg transition ${searchType === 'exact' ? 'bg-orange-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
                    >
                        Точный номер
                    </button>
                    <button
                        onClick={() => setSearchType('name')}
                        className={`px-4 py-2 rounded-lg transition ${searchType === 'name' ? 'bg-orange-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
                    >
                        По названию
                    </button>
                </div>

                <div className="flex gap-3">
                    <input
                        type="text"
                        placeholder={getPlaceholder()}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent text-lg"
                    />
                    <button
                        onClick={handleSearch}
                        className="px-8 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition font-medium text-lg"
                    >
                        Найти
                    </button>
                </div>

                <div className="mt-3">
                    <select
                        value={selectedVehicle}
                        onChange={(e) => setSelectedVehicle(e.target.value)}
                        className="w-full md:w-96 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                    >
                        <option value="">Все автомобили</option>
                        {vehicles.map(vehicle => (
                            <option key={vehicle.id} value={vehicle.id}>
                                {vehicle.make} {vehicle.model} {vehicle.generation ? `(${vehicle.generation})` : ''}
                            </option>
                        ))}
                    </select>
                    {selectedVehicle && (
                        <button
                            onClick={() => setSelectedVehicle('')}
                            className="ml-2 text-sm text-orange-500 hover:text-orange-600"
                        >
                            Сбросить фильтр
                        </button>
                    )}
                </div>
            </div>

            {loading ? (
                <div className="flex justify-center items-center h-64">
                    <div className="text-gray-500">Загрузка...</div>
                </div>
            ) : products.length === 0 ? (
                <div className="text-center text-gray-500 py-10">
                    Товары не найдены
                    {searchQuery && (
                        <div className="mt-2 text-sm">
                            Попробуйте изменить поисковый запрос или тип поиска
                        </div>
                    )}
                </div>
            ) : (
                <>
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {products.map(product => (
                            <ProductCard
                                key={product.id}
                                product={product}
                                onAddToCart={addToCart}
                            />
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div className="flex justify-center gap-3 mt-8 pt-5">
                            <button
                                onClick={() => goToPage(currentPage - 1)}
                                disabled={currentPage === 0}
                                className="px-4 py-2 bg-orange-500 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-orange-600 transition"
                            >
                                ← Назад
                            </button>
                            <span className="px-4 py-2 text-gray-600">
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

            {(externalProducts.length > 0 || loadingExternal) && (
                <div className="mt-12 border-t pt-8">
                    <h2 className="text-xl font-bold mb-4 text-blue-600">Товары от поставщиков (под заказ)</h2>
                    <div className="bg-blue-50 p-4 rounded-lg">
                        <p className="text-sm text-gray-600 mb-3">
                            Эти товары поставляются от наших партнеров. Срок доставки 3-7 дней.
                        </p>
                        {loadingExternal ? (
                            <div className="text-center py-8 text-gray-500">Загрузка...</div>
                        ) : (
                            <div className="space-y-3">
                                {externalProducts.map(product => (
                                    <div key={product.id} className="bg-white p-4 rounded-lg shadow">
                                        <div className="flex justify-between items-start flex-wrap gap-4">
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
                                                    onClick={() => addExternalToCart(product)}
                                                    className="mt-2 bg-orange-500 text-white px-4 py-1.5 rounded-lg text-sm hover:bg-orange-600 transition"
                                                >
                                                    В корзину
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
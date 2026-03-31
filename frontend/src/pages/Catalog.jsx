import React, { useState, useEffect } from 'react';
import api from '../api/api';
import ProductCard from '../components/ProductCard';

function Catalog() {
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

    const goToPage = (page) => {
        if (page >= 0 && page < totalPages) {
            setCurrentPage(page);
        }
    };

    return (
        <div style={{ padding: '20px' }}>
            <h1>Каталог автозапчастей</h1>

            <div style={{
                display: 'flex',
                gap: '10px',
                marginBottom: '20px',
                padding: '15px',
                backgroundColor: '#f5f5f5',
                borderRadius: '8px'
            }}>
                <input
                    type="text"
                    placeholder="Поиск по названию или артикулу..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    style={{
                        flex: 2,
                        padding: '10px',
                        border: '1px solid #ddd',
                        borderRadius: '4px'
                    }}
                />
                <select
                    value={selectedVehicle}
                    onChange={(e) => setSelectedVehicle(e.target.value)}
                    style={{
                        flex: 1,
                        padding: '10px',
                        border: '1px solid #ddd',
                        borderRadius: '4px'
                    }}
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
                    style={{
                        padding: '10px 20px',
                        backgroundColor: '#e67e22',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer'
                    }}
                >
                    Найти
                </button>
            </div>

            {loading ? (
                <p>Загрузка...</p>
            ) : products.length === 0 ? (
                <p>Товары не найдены</p>
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
                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            gap: '10px',
                            marginTop: '30px',
                            padding: '20px'
                        }}>
                            <button
                                onClick={() => goToPage(currentPage - 1)}
                                disabled={currentPage === 0}
                                style={{
                                    padding: '8px 16px',
                                    backgroundColor: '#e67e22',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '4px',
                                    cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
                                    opacity: currentPage === 0 ? 0.5 : 1
                                }}
                            >
                                ← Назад
                            </button>

                            <span style={{ padding: '8px 16px' }}>
                                Страница {currentPage + 1} из {totalPages}
                            </span>

                            <button
                                onClick={() => goToPage(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                                style={{
                                    padding: '8px 16px',
                                    backgroundColor: '#e67e22',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '4px',
                                    cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer',
                                    opacity: currentPage >= totalPages - 1 ? 0.5 : 1
                                }}
                            >
                                Вперед →
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

export default Catalog;
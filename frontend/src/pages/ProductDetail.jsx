import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/api';

function ProductDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);

    useEffect(() => {
        loadProduct();
    }, [id]);

    const loadProduct = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/products/${id}`);
            if (response.data.success) {
                setProduct(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки товара:', error);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async () => {
        try {
            const response = await api.post('/cart/add', {
                productId: product.id,
                quantity: quantity
            });
            if (response.data.success) {
                alert(`${product.name} добавлен в корзину (${quantity} шт)`);
            }
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Ошибка добавления в корзину');
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;
    if (!product) return <div style={{ padding: '20px' }}>Товар не найден</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={() => navigate(-1)} style={{ marginBottom: '20px' }}>
                ← Назад
            </button>

            <div style={{ display: 'flex', gap: '40px', flexWrap: 'wrap' }}>
                <div style={{ flex: 1 }}>
                    <h1 style={{ fontSize: '28px', marginBottom: '10px' }}>{product.name}</h1>
                    <p style={{ color: '#666', marginBottom: '10px' }}>Артикул: {product.sku}</p>
                    <p style={{ color: '#666', marginBottom: '20px' }}>OEM: {product.oemCode || '—'}</p>

                    <div style={{ marginBottom: '20px' }}>
                        <h3>Описание</h3>
                        <p>{product.description || 'Нет описания'}</p>
                    </div>

                    <div style={{ marginBottom: '20px' }}>
                        <h3>Совместимость</h3>
                        {product.compatibleVehicles && product.compatibleVehicles.length > 0 ? (
                            <ul>
                                {product.compatibleVehicles.map((vehicle, idx) => (
                                    <li key={idx}>{vehicle}</li>
                                ))}
                            </ul>
                        ) : (
                            <p>Не указано</p>
                        )}
                    </div>

                    <div style={{ marginBottom: '20px' }}>
                        <h3>Категория</h3>
                        <p>{product.categoryName || '—'}</p>
                    </div>

                    <div style={{ marginBottom: '20px' }}>
                        <h3>Производитель</h3>
                        <p>{product.manufacturerName || '—'}</p>
                    </div>
                </div>

                <div style={{ width: '300px', backgroundColor: '#f5f5f5', padding: '20px', borderRadius: '8px' }}>
                    <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#e67e22', marginBottom: '10px' }}>
                        {product.price} ₽
                    </div>
                    <p style={{ fontSize: '14px', color: product.stock > 0 ? 'green' : 'red', marginBottom: '15px' }}>
                        {product.stock > 0 ? `В наличии: ${product.stock} шт` : 'Нет в наличии'}
                    </p>

                    {product.stock > 0 && (
                        <div style={{ marginBottom: '15px' }}>
                            <label>Количество: </label>
                            <input
                                type="number"
                                min="1"
                                max={product.stock}
                                value={quantity}
                                onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
                                style={{ width: '80px', padding: '8px', marginLeft: '10px' }}
                            />
                        </div>
                    )}

                    <button
                        onClick={addToCart}
                        disabled={product.stock === 0}
                        style={{
                            width: '100%',
                            backgroundColor: '#e67e22',
                            color: 'white',
                            border: 'none',
                            padding: '12px',
                            borderRadius: '4px',
                            fontSize: '16px',
                            cursor: product.stock > 0 ? 'pointer' : 'not-allowed'
                        }}
                    >
                        {product.stock > 0 ? 'В корзину' : 'Нет в наличии'}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ProductDetail;
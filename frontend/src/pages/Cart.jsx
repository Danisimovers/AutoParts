import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';

function Cart() {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const [userId] = useState(2); // Временный ID, потом из сессии
    const navigate = useNavigate();

    useEffect(() => {
        loadCart();
    }, []);

    const loadCart = async () => {
        setLoading(true);
        try {
            const response = await api.get('/cart');
            if (response.data.success) {
                setCart(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки корзины:', error);
        } finally {
            setLoading(false);
        }
    };

    const updateQuantity = async (productId, quantity) => {
        try {
            const response = await api.put('/cart/update', { productId, quantity });
            if (response.data.success) {
                setCart(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка обновления:', error);
        }
    };

    const removeItem = async (productId) => {
        try {
            const response = await api.delete(`/cart/remove/${productId}`);
            if (response.data.success) {
                setCart(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка удаления:', error);
        }
    };

    const clearCart = async () => {
        try {
            const response = await api.delete('/cart/clear');
            if (response.data.success) {
                setCart(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка очистки:', error);
        }
    };

    const checkout = async () => {
        try {
            const response = await api.post('/orders', { userId });
            if (response.data.success) {
                alert('Заказ успешно оформлен!');
                navigate('/order-success', { state: { order: response.data.data } });
            }
        } catch (error) {
            console.error('Ошибка оформления:', error);
            alert(error.response?.data?.message || 'Ошибка оформления заказа');
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;
    if (!cart || cart.items.length === 0) {
        return (
            <div style={{ padding: '20px', textAlign: 'center' }}>
                <h2>Корзина пуста</h2>
                <button onClick={() => navigate('/')} style={{ padding: '10px 20px', cursor: 'pointer' }}>
                    Перейти в каталог
                </button>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px' }}>
            <h1>Корзина</h1>

            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Товар</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Цена</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Количество</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Сумма</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}></th>
                </tr>
                </thead>
                <tbody>
                {cart.items.map(item => (
                    <tr key={item.productId} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '10px' }}>
                            <div>
                                <strong>{item.name}</strong>
                                <div style={{ fontSize: '12px', color: '#666' }}>Артикул: {item.sku}</div>
                            </div>
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>{item.price} ₽</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <input
                                type="number"
                                min="1"
                                value={item.quantity}
                                onChange={(e) => updateQuantity(item.productId, parseInt(e.target.value))}
                                style={{ width: '60px', padding: '5px', textAlign: 'center' }}
                            />
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px', fontWeight: 'bold' }}>
                            {item.total} ₽
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <button
                                onClick={() => removeItem(item.productId)}
                                style={{
                                    backgroundColor: '#ff4444',
                                    color: 'white',
                                    border: 'none',
                                    padding: '5px 10px',
                                    borderRadius: '4px',
                                    cursor: 'pointer'
                                }}
                            >
                                Удалить
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            <div style={{
                marginTop: '20px',
                padding: '20px',
                backgroundColor: '#f5f5f5',
                borderRadius: '8px',
                textAlign: 'right'
            }}>
                <div style={{ fontSize: '18px', marginBottom: '10px' }}>
                    <strong>Итого товаров:</strong> {cart.totalItems} шт
                </div>
                <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#e67e22', marginBottom: '20px' }}>
                    Общая сумма: {cart.totalPrice} ₽
                </div>

                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                    <button
                        onClick={clearCart}
                        style={{
                            padding: '10px 20px',
                            backgroundColor: '#666',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer'
                        }}
                    >
                        Очистить корзину
                    </button>
                    <button
                        onClick={checkout}
                        style={{
                            padding: '10px 20px',
                            backgroundColor: '#e67e22',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '16px'
                        }}
                    >
                        Оформить заказ
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Cart;
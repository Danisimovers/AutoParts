import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

function Cart() {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const { user } = useAuth();
    const userId = user?.id;
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

    const updateQuantity = async (itemId, quantity) => {
        try {
            const response = await api.put('/cart/update', { itemId, quantity });
            if (response.data.success) {
                setCart(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка обновления:', error);
        }
    };

    const removeItem = async (itemId) => {
        try {
            const response = await api.delete(`/cart/remove/${itemId}`);
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

    if (loading) return <div className="p-5">Загрузка...</div>;
    if (!cart || cart.items.length === 0) {
        return (
            <div className="p-5 text-center">
                <h2 className="text-2xl mb-4">Корзина пуста</h2>
                <button onClick={() => navigate('/')} className="bg-orange-500 text-white px-4 py-2 rounded">
                    Перейти в каталог
                </button>
            </div>
        );
    }

    return (
        <div className="p-5">
            <h1 className="text-3xl font-bold mb-6">Корзина</h1>

            <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                    <thead className="bg-gray-100">
                    <tr className="border-b">
                        <th className="text-left p-2">Товар</th>
                        <th className="text-center p-2">Цена</th>
                        <th className="text-center p-2">Количество</th>
                        <th className="text-center p-2">Сумма</th>
                        <th className="text-center p-2"></th>
                    </tr>
                    </thead>
                    <tbody>
                    {cart.items.map(item => (
                        <tr key={item.itemId || item.productId} className="border-b">
                            <td className="p-2">
                                <div>
                                    <strong>{item.name}</strong>
                                    {item.sku && <div className="text-xs text-gray-500">Артикул: {item.sku}</div>}
                                </div>
                            </td>
                            <td className="text-center p-2">{item.price} ₽</td>
                            <td className="text-center p-2">
                                <input
                                    type="number"
                                    min="1"
                                    value={item.quantity}
                                    onChange={(e) => updateQuantity(item.itemId || item.productId, parseInt(e.target.value))}
                                    className="w-16 p-1 text-center border rounded"
                                />
                            </td>
                            <td className="text-center p-2 font-bold">{item.total} ₽</td>
                            <td className="text-center p-2">
                                <button
                                    onClick={() => removeItem(item.itemId || item.productId)}
                                    className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600"
                                >
                                    Удалить
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            <div className="mt-5 p-4 bg-gray-100 rounded-lg text-right">
                <div className="text-lg mb-2">
                    <strong>Итого товаров:</strong> {cart.totalItems} шт
                </div>
                <div className="text-2xl font-bold text-orange-500 mb-4">
                    Общая сумма: {cart.totalPrice} ₽
                </div>
                <div className="flex gap-3 justify-end">
                    <button
                        onClick={clearCart}
                        className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
                    >
                        Очистить корзину
                    </button>
                    <button
                        onClick={checkout}
                        className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600"
                    >
                        Оформить заказ
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Cart;
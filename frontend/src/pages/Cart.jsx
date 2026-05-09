import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';
import { Trash2, ShoppingBag, CreditCard, X, MapPin, Wallet } from 'lucide-react';

function Cart() {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showCheckoutModal, setShowCheckoutModal] = useState(false);
    const [paymentMethod, setPaymentMethod] = useState('cash');
    const [isProcessing, setIsProcessing] = useState(false);
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
        if (quantity < 1) return;
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
        if (!window.confirm('Очистить корзину?')) return;
        try {
            const response = await api.delete('/cart/clear');
            if (response.data.success) {
                setCart(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка очистки:', error);
        }
    };

    const handleCheckout = () => {
        setShowCheckoutModal(true);
    };

    const confirmOrder = async () => {
        setIsProcessing(true);

        try {
            const response = await api.post('/orders', { userId });
            if (response.data.success) {
                setShowCheckoutModal(false);
                alert('Заказ успешно оформлен!');
                navigate('/order-success', { state: { order: response.data.data, paymentMethod } });
            }
        } catch (error) {
            console.error('Ошибка оформления:', error);
            alert(error.response?.data?.message || 'Ошибка оформления заказа');
        } finally {
            setIsProcessing(false);
        }
    };

    if (loading) return (
        <div className="flex justify-center items-center h-64">
            <div className="text-gray-400">Загрузка...</div>
        </div>
    );

    if (!cart || cart.items.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <ShoppingBag size={64} className="text-gray-300 mb-4" />
                <h2 className="text-2xl text-gray-500 mb-4">Корзина пуста</h2>
                <button
                    onClick={() => navigate('/')}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition"
                >
                    Перейти в каталог
                </button>
            </div>
        );
    }

    return (
        <div className="max-w-5xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-8 flex items-center gap-3">
                <ShoppingBag size={28} className="text-orange-500" />
                Корзина
                <span className="text-sm font-normal text-gray-400 ml-2">
                    {cart.totalItems} {cart.totalItems === 1 ? 'товар' : 'товаров'}
                </span>
            </h1>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-4">
                    {cart.items.map(item => (
                        <div key={item.itemId || item.productId} className="bg-white rounded-xl border border-gray-100 shadow-sm p-4 hover:shadow-md transition">
                            <div className="flex flex-col sm:flex-row gap-4">
                                <div className="flex-1">
                                    <h3 className="font-semibold text-gray-800">{item.name}</h3>
                                    {item.sku && (
                                        <p className="text-sm text-gray-400">Артикул: {item.sku}</p>
                                    )}
                                    <div className="mt-2 sm:hidden">
                                        <div className="text-xl font-bold text-orange-500">{item.price} ₽</div>
                                    </div>
                                </div>

                                <div className="flex items-center justify-between sm:justify-end gap-6 flex-wrap sm:flex-nowrap">
                                    <div className="text-right hidden sm:block">
                                        <div className="text-xl font-bold text-orange-500">{item.price} ₽</div>
                                    </div>

                                    <div className="flex items-center gap-2">
                                        <button
                                            onClick={() => updateQuantity(item.itemId || item.productId, item.quantity - 1)}
                                            className="w-8 h-8 rounded-lg border border-gray-200 flex items-center justify-center hover:border-orange-300 hover:text-orange-500 transition"
                                        >
                                            -
                                        </button>
                                        <span className="w-12 text-center font-medium">{item.quantity}</span>
                                        <button
                                            onClick={() => updateQuantity(item.itemId || item.productId, item.quantity + 1)}
                                            className="w-8 h-8 rounded-lg border border-gray-200 flex items-center justify-center hover:border-orange-300 hover:text-orange-500 transition"
                                        >
                                            +
                                        </button>
                                    </div>

                                    <div className="text-right min-w-[100px]">
                                        <div className="font-bold text-gray-800">{item.total} ₽</div>
                                    </div>

                                    <button
                                        onClick={() => removeItem(item.itemId || item.productId)}
                                        className="text-gray-400 hover:text-red-500 transition"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="lg:col-span-1">
                    <div className="bg-gray-50 rounded-xl p-6 sticky top-4">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4">Итого</h3>

                        <div className="space-y-3 mb-6">
                            <div className="flex justify-between text-gray-600">
                                <span>Товаров:</span>
                                <span>{cart.totalItems} шт</span>
                            </div>
                            <div className="flex justify-between text-gray-600">
                                <span>Сумма:</span>
                                <span className="font-medium">{cart.totalPrice} ₽</span>
                            </div>
                            <div className="border-t pt-3 mt-3">
                                <div className="flex justify-between text-xl font-bold text-gray-800">
                                    <span>К оплате:</span>
                                    <span className="text-orange-500">{cart.totalPrice} ₽</span>
                                </div>
                            </div>
                        </div>

                        <button
                            onClick={handleCheckout}
                            className="w-full bg-orange-500 text-white py-3 rounded-xl font-medium flex items-center justify-center gap-2 hover:bg-orange-600 transition"
                        >
                            <CreditCard size={18} />
                            Оформить заказ
                        </button>

                        <button
                            onClick={clearCart}
                            className="w-full mt-3 bg-white border border-gray-200 text-gray-600 py-3 rounded-xl font-medium flex items-center justify-center gap-2 hover:bg-gray-50 hover:border-gray-300 transition"
                        >
                            <X size={18} />
                            Очистить корзину
                        </button>
                    </div>
                </div>
            </div>

            {/* Модальное окно оформления заказа */}
            {showCheckoutModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6 border-b border-gray-100 flex justify-between items-center sticky top-0 bg-white">
                            <h2 className="text-xl font-bold text-gray-800">Оформление заказа</h2>
                            <button
                                onClick={() => setShowCheckoutModal(false)}
                                className="text-gray-400 hover:text-gray-600 transition"
                            >
                                <X size={24} />
                            </button>
                        </div>

                        <div className="p-6 space-y-6">
                            {/* Товары в заказе */}
                            <div>
                                <h3 className="font-semibold text-gray-800 mb-3">Товары в заказе</h3>
                                <div className="space-y-2 max-h-64 overflow-y-auto">
                                    {cart.items.map(item => (
                                        <div key={item.itemId || item.productId} className="flex justify-between text-sm py-2 border-b border-gray-100">
                                            <div>
                                                <span className="font-medium">{item.name}</span>
                                                <span className="text-gray-400 ml-2">x{item.quantity}</span>
                                            </div>
                                            <span className="font-medium">{item.total} ₽</span>
                                        </div>
                                    ))}
                                </div>
                                <div className="flex justify-between pt-3 mt-2 border-t">
                                    <span className="font-bold">Итого:</span>
                                    <span className="font-bold text-orange-500 text-lg">{cart.totalPrice} ₽</span>
                                </div>
                            </div>

                            {/* Способ получения */}
                            <div className="bg-green-50 rounded-xl p-4">
                                <div className="flex items-start gap-3">
                                    <MapPin size={20} className="text-green-600 mt-0.5" />
                                    <div>
                                        <h4 className="font-semibold text-gray-800">Самовывоз</h4>
                                        <p className="text-sm text-gray-600">Казачий рынок, Ростовская область, Октябрьский район, М-4 Дон, 1005-й километр</p>
                                        <p className="text-xs text-gray-400 mt-1">Режим работы: Пн-Вс 07:00-21:00</p>
                                    </div>
                                </div>
                            </div>

                            {/* Способ оплаты */}
                            <div>
                                <h3 className="font-semibold text-gray-800 mb-3">Способ оплаты</h3>
                                <div className="grid grid-cols-1 gap-3">
                                    <label className={`flex items-center gap-3 p-3 border rounded-xl cursor-pointer transition ${paymentMethod === 'cash' ? 'border-orange-500 bg-orange-50' : 'border-gray-200 hover:border-orange-200'}`}>
                                        <input
                                            type="radio"
                                            name="paymentMethod"
                                            value="cash"
                                            checked={paymentMethod === 'cash'}
                                            onChange={(e) => setPaymentMethod(e.target.value)}
                                            className="w-4 h-4 text-orange-500"
                                        />
                                        <Wallet size={18} className="text-gray-500" />
                                        <div>
                                            <p className="text-sm font-medium">Оплата при получении</p>
                                            <p className="text-xs text-gray-400">Наличными или картой на складе</p>
                                        </div>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div className="p-6 border-t border-gray-100 flex gap-3">
                            <button
                                onClick={() => setShowCheckoutModal(false)}
                                className="flex-1 px-4 py-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition"
                            >
                                Отмена
                            </button>
                            <button
                                onClick={confirmOrder}
                                disabled={isProcessing}
                                className="flex-1 px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition disabled:opacity-50"
                            >
                                {isProcessing ? 'Обработка...' : 'Подтвердить заказ'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Cart;
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

function MyOrders() {
    const { user } = useAuth();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showReturnModal, setShowReturnModal] = useState(false);
    const [selectedOrderItem, setSelectedOrderItem] = useState(null);
    const [returnReason, setReturnReason] = useState('');
    const [sending, setSending] = useState(false);

    useEffect(() => {
        if (user) {
            loadOrders();
        }
    }, [user]);

    const loadOrders = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/orders/user/${user.id}`);
            if (response.data.success) {
                setOrders(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки заказов:', error);
        } finally {
            setLoading(false);
        }
    };

    const openReturnModal = (orderItem) => {
        setSelectedOrderItem(orderItem);
        setReturnReason('');
        setShowReturnModal(true);
    };

    const submitReturn = async () => {
        if (!returnReason.trim()) {
            alert('Укажите причину возврата');
            return;
        }

        setSending(true);
        try {
            const response = await api.post('/returns', {
                orderItemId: selectedOrderItem.id,
                reason: returnReason
            });
            if (response.data.success) {
                alert('Заявка на возврат отправлена');
                setShowReturnModal(false);
                setReturnReason('');
            } else {
                alert(response.data.message);
            }
        } catch (error) {
            alert(error.response?.data?.message || 'Ошибка отправки заявки');
        } finally {
            setSending(false);
        }
    };

    const getStatusColor = (status) => {
        switch(status) {
            case 'CREATED': return 'bg-yellow-500';
            case 'PAID': return 'bg-blue-500';
            case 'SHIPPED': return 'bg-purple-500';
            case 'DELIVERED': return 'bg-green-500';
            case 'CANCELLED': return 'bg-red-500';
            default: return 'bg-gray-500';
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'CREATED': return 'Создан';
            case 'PAID': return 'Оплачен';
            case 'SHIPPED': return 'Отправлен';
            case 'DELIVERED': return 'Доставлен';
            case 'CANCELLED': return 'Отменен';
            default: return status;
        }
    };

    if (!user) {
        return (
            <div className="p-5 text-center">
                <h2 className="text-2xl mb-4">Для просмотра заказов необходимо войти</h2>
                <Link to="/login" className="text-orange-500">Войти</Link>
            </div>
        );
    }

    if (loading) return <div className="p-5">Загрузка...</div>;

    if (orders.length === 0) {
        return (
            <div className="p-5 text-center">
                <h2 className="text-2xl mb-4">У вас пока нет заказов</h2>
                <Link to="/" className="text-orange-500">Перейти в каталог</Link>
            </div>
        );
    }

    return (
        <div className="p-5">
            <h1 className="text-3xl font-bold mb-6">Мои заказы</h1>

            {orders.map(order => (
                <div key={order.id} className="border rounded-lg mb-5 p-4 bg-gray-50">
                    <div className="flex justify-between items-center mb-4">
                        <div>
                            <strong className="text-lg">Заказ #{order.id}</strong>
                            <span className="ml-4 text-gray-500">
                                {new Date(order.createdAt).toLocaleDateString()}
                            </span>
                        </div>
                        <div>
                            <span className={`${getStatusColor(order.status)} text-white px-3 py-1 rounded-full text-xs`}>
                                {getStatusText(order.status)}
                            </span>
                        </div>
                    </div>

                    <table className="w-full border-collapse">
                        <thead>
                        <tr className="border-b border-gray-300">
                            <th className="text-left p-2">Товар</th>
                            <th className="text-center p-2">Цена</th>
                            <th className="text-center p-2">Количество</th>
                            <th className="text-center p-2">Сумма</th>
                            <th className="text-center p-2"></th>
                        </tr>
                        </thead>
                        <tbody>
                        {order.items && order.items.map(item => (
                            <tr key={item.id} className="border-b border-gray-200">
                                <td className="p-2">
                                    <div>
                                        <strong>{item.productName}</strong>
                                        <div className="text-xs text-gray-500">Артикул: {item.productSku}</div>
                                    </div>
                                </td>
                                <td className="text-center p-2">{item.price} ₽</td>
                                <td className="text-center p-2">{item.quantity}</td>
                                <td className="text-center p-2 font-bold">{item.total} ₽</td>
                                <td className="text-center p-2">
                                    {order.status === 'DELIVERED' && (
                                        <button
                                            onClick={() => openReturnModal(item)}
                                            className="bg-orange-500 text-white px-3 py-1 rounded cursor-pointer hover:bg-orange-600"
                                        >
                                            Вернуть
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <div className="text-right mt-4 font-bold">
                        Итого: {order.total} ₽
                    </div>
                </div>
            ))}

            {/* Модальное окно */}
            {showReturnModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-[1000]">
                    <div className="bg-white p-8 rounded-lg max-w-md w-[90%]">
                        <h2 className="text-xl font-bold mb-4">Оформление возврата</h2>
                        <p className="mb-4">Товар: <strong>{selectedOrderItem?.productName}</strong></p>
                        <div className="mb-4">
                            <label className="block mb-1">Причина возврата *</label>
                            <textarea
                                value={returnReason}
                                onChange={(e) => setReturnReason(e.target.value)}
                                rows="4"
                                placeholder="Опишите причину возврата..."
                                className="w-full p-2 border border-gray-300 rounded resize-y"
                            />
                        </div>
                        <div className="flex gap-2 justify-end">
                            <button
                                onClick={() => setShowReturnModal(false)}
                                className="bg-gray-500 text-white px-4 py-2 rounded cursor-pointer hover:bg-gray-600"
                            >
                                Отмена
                            </button>
                            <button
                                onClick={submitReturn}
                                disabled={sending}
                                className="bg-orange-500 text-white px-4 py-2 rounded cursor-pointer hover:bg-orange-600 disabled:opacity-50"
                            >
                                {sending ? 'Отправка...' : 'Отправить'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default MyOrders;
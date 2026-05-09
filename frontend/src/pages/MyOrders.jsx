import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';
import { Package, Calendar, CreditCard, RotateCcw, X, ChevronDown, ChevronUp } from 'lucide-react';

function MyOrders() {
    const { user } = useAuth();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showReturnModal, setShowReturnModal] = useState(false);
    const [selectedOrderItem, setSelectedOrderItem] = useState(null);
    const [returnReason, setReturnReason] = useState('');
    const [sending, setSending] = useState(false);
    const [expandedOrders, setExpandedOrders] = useState({});

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
                // По умолчанию все заказы раскрыты
                const expanded = {};
                response.data.data.forEach(order => {
                    expanded[order.id] = true;
                });
                setExpandedOrders(expanded);
            }
        } catch (error) {
            console.error('Ошибка загрузки заказов:', error);
        } finally {
            setLoading(false);
        }
    };

    const toggleOrder = (orderId) => {
        setExpandedOrders(prev => ({
            ...prev,
            [orderId]: !prev[orderId]
        }));
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
            case 'CREATED': return 'bg-yellow-100 text-yellow-800';
            case 'PAID': return 'bg-blue-100 text-blue-800';
            case 'SHIPPED': return 'bg-purple-100 text-purple-800';
            case 'DELIVERED': return 'bg-green-100 text-green-800';
            case 'CANCELLED': return 'bg-red-100 text-red-800';
            case 'PENDING_SUPPLIER': return 'bg-orange-100 text-orange-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'CREATED': return 'Создан';
            case 'PAID': return 'Оплачен';
            case 'SHIPPED': return 'Отправлен';
            case 'DELIVERED': return 'Доставлен';
            case 'CANCELLED': return 'Отменён';
            case 'PENDING_SUPPLIER': return 'Ожидает поставки';
            default: return status;
        }
    };

    if (!user) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <Package size={64} className="text-gray-300 mb-4" />
                <h2 className="text-2xl text-gray-500 mb-4">Для просмотра заказов необходимо войти</h2>
                <Link to="/login" className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition">
                    Войти
                </Link>
            </div>
        );
    }

    if (loading) return (
        <div className="flex justify-center items-center h-64">
            <div className="text-gray-400">Загрузка...</div>
        </div>
    );

    if (orders.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <Package size={64} className="text-gray-300 mb-4" />
                <h2 className="text-2xl text-gray-500 mb-4">У вас пока нет заказов</h2>
                <Link to="/" className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition">
                    Перейти в каталог
                </Link>
            </div>
        );
    }

    return (
        <div className="w-full px-4 py-8">
            <div className="max-w-[1400px] mx-auto">
                <h1 className="text-3xl font-bold text-gray-800 mb-8 flex items-center gap-3">
                    <Package size={28} className="text-orange-500" />
                    Мои заказы
                    <span className="text-sm font-normal text-gray-400 ml-2">
                        {orders.length} {orders.length === 1 ? 'заказ' : 'заказа'}
                    </span>
                </h1>

                <div className="space-y-4">
                    {orders.map(order => (
                        <div key={order.id} className="bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow overflow-hidden">
                            {/* Шапка заказа - кликабельная */}
                            <div
                                className="px-6 py-5 bg-gradient-to-r from-gray-50 to-white border-b border-gray-200 cursor-pointer hover:bg-gray-50 transition"
                                onClick={() => toggleOrder(order.id)}
                            >
                                <div className="flex flex-wrap items-center justify-between gap-4">
                                    <div className="flex items-center gap-6 flex-wrap">
                                        <div className="flex items-center gap-3">
                                            <div className="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center">
                                                <Package size={18} className="text-orange-500" />
                                            </div>
                                            <div>
                                                <p className="text-sm text-gray-500">Заказ №</p>
                                                <p className="font-bold text-gray-800 text-lg">{order.id}</p>
                                            </div>
                                        </div>

                                        <div className="h-10 w-px bg-gray-200"></div>

                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} className="text-gray-400" />
                                            <div>
                                                <p className="text-sm text-gray-500">Дата</p>
                                                <p className="font-medium text-gray-700">
                                                    {new Date(order.createdAt).toLocaleDateString('ru-RU', {
                                                        day: 'numeric',
                                                        month: 'long',
                                                        year: 'numeric'
                                                    })}
                                                </p>
                                            </div>
                                        </div>

                                        <div className="h-10 w-px bg-gray-200"></div>

                                        <div className="flex items-center gap-2">
                                            <CreditCard size={16} className="text-gray-400" />
                                            <div>
                                                <p className="text-sm text-gray-500">Сумма</p>
                                                <p className="font-bold text-gray-800 text-xl">{order.total.toLocaleString()} ₽</p>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-4">
                                        <span className={`px-4 py-2 rounded-full text-sm font-medium ${getStatusColor(order.status)}`}>
                                            {getStatusText(order.status)}
                                        </span>
                                        {expandedOrders[order.id] ? (
                                            <ChevronUp size={20} className="text-gray-400" />
                                        ) : (
                                            <ChevronDown size={20} className="text-gray-400" />
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Тело заказа - раскрывается */}
                            {expandedOrders[order.id] && (
                                <div className="p-6">
                                    <div className="overflow-x-auto">
                                        <table className="w-full">
                                            <thead>
                                            <tr className="border-b-2 border-gray-100">
                                                <th className="px-4 py-4 text-left text-sm font-semibold text-gray-500 uppercase tracking-wider">
                                                    Товар
                                                </th>
                                                <th className="px-4 py-4 text-right text-sm font-semibold text-gray-500 uppercase tracking-wider w-28">
                                                    Цена
                                                </th>
                                                <th className="px-4 py-4 text-center text-sm font-semibold text-gray-500 uppercase tracking-wider w-24">
                                                    Кол-во
                                                </th>
                                                <th className="px-4 py-4 text-right text-sm font-semibold text-gray-500 uppercase tracking-wider w-32">
                                                    Сумма
                                                </th>
                                                <th className="px-4 py-4 text-center text-sm font-semibold text-gray-500 uppercase tracking-wider w-32">

                                                </th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            {order.items && order.items.map((item, idx) => (
                                                <tr key={item.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                                                    <td className="px-4 py-5">
                                                        <div>
                                                            <p className="font-semibold text-gray-800 text-base">
                                                                {item.productName || 'Товар поставщика'}
                                                            </p>
                                                            {item.productSku && (
                                                                <p className="text-sm text-gray-400 mt-1">
                                                                    Артикул: {item.productSku}
                                                                </p>
                                                            )}
                                                        </div>
                                                    </td>
                                                    <td className="px-4 py-5 text-right text-gray-700 font-medium">
                                                        {item.price.toLocaleString()} ₽
                                                    </td>
                                                    <td className="px-4 py-5 text-center">
                                                        <div className="inline-flex items-center justify-center px-3 py-1 bg-gray-100 rounded-lg text-gray-700 font-medium">
                                                            {item.quantity}
                                                        </div>
                                                    </td>
                                                    <td className="px-4 py-5 text-right font-bold text-gray-800 text-lg">
                                                        {item.total.toLocaleString()} ₽
                                                    </td>
                                                    <td className="px-4 py-5 text-center">
                                                        {order.status === 'DELIVERED' && (
                                                            <button
                                                                onClick={() => openReturnModal(item)}
                                                                className="flex items-center gap-2 px-4 py-2 text-sm bg-gray-100 text-gray-600 rounded-xl hover:bg-orange-500 hover:text-white transition-all duration-200 mx-auto"
                                                            >
                                                                <RotateCcw size={14} />
                                                                Вернуть
                                                            </button>
                                                        )}
                                                    </td>
                                                </tr>
                                            ))}
                                            </tbody>
                                            <tfoot>
                                            <tr className="border-t-2 border-gray-200 bg-gray-50">
                                                <td colSpan="3" className="px-4 py-5 text-right">
                                                    <div className="flex items-center justify-end gap-8">
                                                            <span className="text-base font-medium text-gray-600">
                                                                Итого к оплате:
                                                            </span>
                                                    </div>
                                                </td>
                                                <td colSpan="2" className="px-4 py-5">
                                                    <div className="text-right">
                                                            <span className="text-2xl font-bold text-orange-600">
                                                                {order.total.toLocaleString()} ₽
                                                            </span>
                                                    </div>
                                                </td>
                                            </tr>
                                            </tfoot>
                                        </table>
                                    </div>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>

            {/* Модальное окно возврата */}
            {showReturnModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl">
                        <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                            <h2 className="text-xl font-bold text-gray-800">Оформление возврата</h2>
                            <button
                                onClick={() => setShowReturnModal(false)}
                                className="text-gray-400 hover:text-gray-600 transition"
                            >
                                <X size={24} />
                            </button>
                        </div>

                        <div className="p-6 space-y-4">
                            <div className="bg-orange-50 p-4 rounded-xl">
                                <p className="text-gray-700">
                                    Товар: <span className="font-semibold">{selectedOrderItem?.productName}</span>
                                </p>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Причина возврата <span className="text-red-500">*</span>
                                </label>
                                <textarea
                                    value={returnReason}
                                    onChange={(e) => setReturnReason(e.target.value)}
                                    rows="4"
                                    placeholder="Опишите причину возврата..."
                                    className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent resize-y"
                                />
                            </div>
                        </div>

                        <div className="p-6 border-t border-gray-100 flex gap-3">
                            <button
                                onClick={() => setShowReturnModal(false)}
                                className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-600 rounded-xl hover:bg-gray-200 transition font-medium"
                            >
                                Отмена
                            </button>
                            <button
                                onClick={submitReturn}
                                disabled={sending}
                                className="flex-1 px-4 py-2.5 bg-orange-500 text-white rounded-xl hover:bg-orange-600 transition disabled:opacity-50 font-medium"
                            >
                                {sending ? 'Отправка...' : 'Отправить заявку'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default MyOrders;
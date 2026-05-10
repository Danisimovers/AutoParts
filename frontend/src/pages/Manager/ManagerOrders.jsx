import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function ManagerOrders() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadOrders();
    }, []);

    const loadOrders = async () => {
        setLoading(true);
        try {
            const response = await api.get('/manager/orders');
            if (response.data.success) {
                setOrders(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки заказов:', error);
            alert('Ошибка загрузки заказов');
        } finally {
            setLoading(false);
        }
    };

    const updateStatus = async (id, status) => {
        try {
            await api.put(`/manager/orders/${id}/status?status=${status}`);
            loadOrders();
            alert('Статус заказа обновлен');
        } catch (error) {
            alert('Ошибка обновления статуса');
        }
    };

    const getStatusClass = (status) => {
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

    if (loading) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Управление заказами</h1>
                <p className="text-sm text-gray-500 mt-1">Всего заказов: {orders.length}</p>
            </div>

            <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                <table className="w-full">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID заказа</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID пользователя</th>
                        <th className="text-right p-3 text-sm font-semibold text-gray-600">Сумма</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Дата</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Статус</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Действие</th>
                    </tr>
                    </thead>
                    <tbody>
                    {orders.map(order => (
                        <tr key={order.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm font-medium text-gray-800">#{order.id}</td>
                            <td className="p-3 text-sm text-gray-600">{order.userId}</td>
                            <td className="p-3 text-right text-sm font-semibold text-gray-800">{order.total.toLocaleString()} ₽</td>
                            <td className="p-3 text-sm text-gray-600">
                                {new Date(order.createdAt).toLocaleDateString()}
                            </td>
                            <td className="text-center p-3">
                                    <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusClass(order.status)}`}>
                                        {getStatusText(order.status)}
                                    </span>
                            </td>
                            <td className="text-center p-3">
                                <select
                                    onChange={(e) => updateStatus(order.id, e.target.value)}
                                    defaultValue={order.status}
                                    className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                >
                                    <option value="CREATED">Создан</option>
                                    <option value="PAID">Оплачен</option>
                                    <option value="SHIPPED">Отправлен</option>
                                    <option value="DELIVERED">Доставлен</option>
                                    <option value="CANCELLED">Отменён</option>
                                    <option value="PENDING_SUPPLIER">Ожидает поставки</option>
                                </select>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default ManagerOrders;
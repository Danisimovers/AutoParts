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

    const getStatusColor = (status) => {
        switch(status) {
            case 'CREATED': return 'bg-yellow-500';
            case 'PAID': return 'bg-blue-500';
            case 'SHIPPED': return 'bg-purple-500';
            case 'DELIVERED': return 'bg-green-500';
            case 'CANCELLED': return 'bg-red-500';
            case 'PENDING_SUPPLIER': return 'bg-purple-500';
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
            case 'PENDING_SUPPLIER': return 'Ожидает поставки';
            default: return status;
        }
    };

    if (loading) return <div className="p-5">Загрузка...</div>;

    return (
        <div className="p-5">
            <h1 className="text-2xl font-bold mb-5">Управление заказами</h1>
            <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                    <thead className="bg-gray-100">
                    <tr className="border-b">
                        <th className="text-left p-2">ID заказа</th>
                        <th className="text-left p-2">ID пользователя</th>
                        <th className="text-left p-2">Сумма</th>
                        <th className="text-left p-2">Дата</th>
                        <th className="text-center p-2">Статус</th>
                        <th className="text-center p-2">Действие</th>
                    </tr>
                    </thead>
                    <tbody>
                    {orders.map(order => (
                        <tr key={order.id} className="border-b">
                            <td className="p-2">#{order.id}</td>
                            <td className="p-2">{order.userId}</td>
                            <td className="p-2">{order.total} ₽</td>
                            <td className="p-2">{new Date(order.createdAt).toLocaleDateString()}</td>
                            <td className="text-center p-2">
                                    <span className={`${getStatusColor(order.status)} text-white px-2 py-1 rounded-full text-xs`}>
                                        {getStatusText(order.status)}
                                    </span>
                            </td>
                            <td className="text-center p-2">
                                <select
                                    onChange={(e) => updateStatus(order.id, e.target.value)}
                                    defaultValue={order.status}
                                    className="p-1 border rounded text-sm"
                                >
                                    <option value="CREATED">Создан</option>
                                    <option value="PAID">Оплачен</option>
                                    <option value="SHIPPED">Отправлен</option>
                                    <option value="DELIVERED">Доставлен</option>
                                    <option value="CANCELLED">Отменен</option>
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
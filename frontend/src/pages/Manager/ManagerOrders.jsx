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
            case 'CREATED': return '#ff9800';
            case 'PAID': return '#2196f3';
            case 'SHIPPED': return '#9c27b0';
            case 'DELIVERED': return '#4caf50';
            case 'CANCELLED': return '#f44336';
            default: return '#666';
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

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    return (
        <div>
            <h1>Управление заказами</h1>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>ID заказа</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>ID пользователя</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Сумма</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Дата</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Статус</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Действие</th>
                </tr>
                </thead>
                <tbody>
                {orders.map(order => (
                    <tr key={order.id} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '10px' }}>#{order.id}</td>
                        <td style={{ padding: '10px' }}>{order.userId}</td>
                        <td style={{ padding: '10px' }}>{order.total} ₽</td>
                        <td style={{ padding: '10px' }}>{new Date(order.createdAt).toLocaleDateString()}</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                                <span style={{
                                    backgroundColor: getStatusColor(order.status),
                                    color: 'white',
                                    padding: '4px 12px',
                                    borderRadius: '20px',
                                    fontSize: '12px'
                                }}>
                                    {getStatusText(order.status)}
                                </span>
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <select
                                onChange={(e) => updateStatus(order.id, e.target.value)}
                                defaultValue={order.status}
                                style={{ padding: '5px 10px', borderRadius: '4px', border: '1px solid #ddd' }}
                            >
                                <option value="CREATED">Создан</option>
                                <option value="PAID">Оплачен</option>
                                <option value="SHIPPED">Отправлен</option>
                                <option value="DELIVERED">Доставлен</option>
                                <option value="CANCELLED">Отменен</option>
                            </select>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default ManagerOrders;
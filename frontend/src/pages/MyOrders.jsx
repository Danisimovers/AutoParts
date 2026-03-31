import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

function MyOrders() {
    const { user } = useAuth();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);

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

    if (!user) {
        return (
            <div style={{ padding: '20px', textAlign: 'center' }}>
                <h2>Для просмотра заказов необходимо войти</h2>
                <Link to="/login">Войти</Link>
            </div>
        );
    }

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    if (orders.length === 0) {
        return (
            <div style={{ padding: '20px', textAlign: 'center' }}>
                <h2>У вас пока нет заказов</h2>
                <Link to="/" style={{ color: '#e67e22' }}>Перейти в каталог</Link>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px' }}>
            <h1>Мои заказы</h1>

            <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                    <tr style={{ borderBottom: '2px solid #ddd' }}>
                        <th style={{ textAlign: 'left', padding: '12px' }}>№ заказа</th>
                        <th style={{ textAlign: 'center', padding: '12px' }}>Дата</th>
                        <th style={{ textAlign: 'center', padding: '12px' }}>Сумма</th>
                        <th style={{ textAlign: 'center', padding: '12px' }}>Статус</th>
                        <th style={{ textAlign: 'center', padding: '12px' }}></th>
                    </tr>
                    </thead>
                    <tbody>
                    {orders.map(order => (
                        <tr key={order.id} style={{ borderBottom: '1px solid #eee' }}>
                            <td style={{ padding: '12px' }}>
                                <strong>#{order.id}</strong>
                            </td>
                            <td style={{ textAlign: 'center', padding: '12px' }}>
                                {new Date(order.createdAt).toLocaleDateString('ru-RU')}
                            </td>
                            <td style={{ textAlign: 'center', padding: '12px', fontWeight: 'bold' }}>
                                {order.total} ₽
                            </td>
                            <td style={{ textAlign: 'center', padding: '12px' }}>
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
                            <td style={{ textAlign: 'center', padding: '12px' }}>
                                <Link
                                    to={`/order/${order.id}`}
                                    style={{
                                        color: '#e67e22',
                                        textDecoration: 'none'
                                    }}
                                >
                                    Подробнее
                                </Link>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default MyOrders;
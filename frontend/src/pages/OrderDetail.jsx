import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/api';

function OrderDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadOrder();
    }, [id]);

    const loadOrder = async () => {
        setLoading(true);
        try {
            const orderResponse = await api.get(`/orders/${id}`);
            if (orderResponse.data.success) {
                setOrder(orderResponse.data.data);

                const itemsResponse = await api.get(`/orders/${id}/items`);
                if (itemsResponse.data.success) {
                    setItems(itemsResponse.data.data);
                }
            }
        } catch (error) {
            console.error('Ошибка загрузки заказа:', error);
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

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;
    if (!order) return <div style={{ padding: '20px' }}>Заказ не найден</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={() => navigate('/my-orders')} style={{ marginBottom: '20px' }}>
                ← Назад к заказам
            </button>

            <h1>Заказ #{order.id}</h1>

            <div style={{ marginBottom: '20px', padding: '15px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                <p><strong>Дата:</strong> {new Date(order.createdAt).toLocaleString('ru-RU')}</p>
                <p><strong>Статус:</strong>
                    <span style={{
                        backgroundColor: '#e67e22',
                        color: 'white',
                        padding: '4px 12px',
                        borderRadius: '20px',
                        fontSize: '12px',
                        marginLeft: '10px'
                    }}>
                        {getStatusText(order.status)}
                    </span>
                </p>
                <p><strong>Общая сумма:</strong> <span style={{ fontSize: '20px', fontWeight: 'bold', color: '#e67e22' }}>{order.total} ₽</span></p>
            </div>

            <h2>Товары в заказе</h2>

            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Товар</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Цена</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Количество</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Сумма</th>
                </tr>
                </thead>
                <tbody>
                {items.map(item => (
                    <tr key={item.id} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '10px' }}>
                            <div>
                                <strong>{item.productName}</strong>
                                <div style={{ fontSize: '12px', color: '#666' }}>Артикул: {item.productSku}</div>
                            </div>
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>{item.price} ₽</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>{item.quantity}</td>
                        <td style={{ textAlign: 'center', padding: '10px', fontWeight: 'bold' }}>{item.total} ₽</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default OrderDetail;
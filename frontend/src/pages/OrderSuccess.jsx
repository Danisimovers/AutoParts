import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

function OrderSuccess() {
    const location = useLocation();
    const navigate = useNavigate();
    const { order } = location.state || {};

    return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
            <div style={{
                backgroundColor: '#d4edda',
                color: '#155724',
                padding: '20px',
                borderRadius: '8px',
                marginBottom: '20px'
            }}>
                <h1>✅ Заказ успешно оформлен!</h1>
                <p>Номер заказа: {order?.id}</p>
                <p>Сумма заказа: {order?.total} ₽</p>
                <p>Статус: {order?.status}</p>
            </div>

            <button
                onClick={() => navigate('/')}
                style={{
                    padding: '10px 20px',
                    backgroundColor: '#e67e22',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                }}
            >
                Продолжить покупки
            </button>
        </div>
    );
}

export default OrderSuccess;
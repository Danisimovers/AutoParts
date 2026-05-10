import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import api from '../api/api';

function VerifyEmail() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const [status, setStatus] = useState('loading');
    const [message, setMessage] = useState('');

    useEffect(() => {
        if (token) {
            verifyEmail();
        } else {
            setStatus('error');
            setMessage('Токен подтверждения не найден');
        }
    }, [token]);

    const verifyEmail = async () => {
        try {
            const response = await api.post(`/auth/verify?token=${token}`);
            if (response.data.success) {
                setStatus('success');
                setMessage('Email успешно подтвержден! Теперь вы можете войти.');

                // Если бэкенд вернул токен и данные пользователя — сохраняем
                if (response.data.data?.token) {
                    localStorage.setItem('token', response.data.data.token);
                    localStorage.setItem('user', JSON.stringify(response.data.data));
                }
            } else {
                setStatus('error');
                setMessage(response.data.message);
            }
        } catch (error) {
            console.error('Ошибка подтверждения:', error);
            setStatus('error');
            setMessage(error.response?.data?.message || 'Ошибка подтверждения email');
        }
    };

    if (status === 'loading') {
        return (
            <div style={{ padding: '20px', textAlign: 'center' }}>
                <h2>Подтверждение email...</h2>
                <p>Пожалуйста, подождите</p>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px', maxWidth: '500px', margin: '50px auto', textAlign: 'center' }}>
            <div style={{
                backgroundColor: status === 'success' ? '#d4edda' : '#ffebee',
                color: status === 'success' ? '#155724' : '#c62828',
                padding: '30px',
                borderRadius: '8px'
            }}>
                <h2>{status === 'success' ? ' Подтверждено!' : 'Ошибка'}</h2>
                <p>{message}</p>
                {status === 'success' && (
                    <Link to="/login" style={{ display: 'inline-block', marginTop: '15px', color: '#e67e22' }}>
                        Перейти к входу
                    </Link>
                )}
            </div>
        </div>
    );
}

export default VerifyEmail;
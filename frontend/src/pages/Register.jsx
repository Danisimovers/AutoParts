import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';

function Register() {
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);

    // Валидация email
    const validateEmail = (email) => {
        const emailRegex = /^[^\s@]+@([^\s@.,]+\.)+[^\s@.,]{2,}$/;
        return emailRegex.test(email);
    };

    const validatePhone = (phone) => {
        if (!phone) return true;
        const cleaned = phone.replace(/[^\d+]/g, '');
        const phoneRegex = /^(\+7|8)?9\d{9}$/;
        return phoneRegex.test(cleaned);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (password !== confirmPassword) {
            setError('Пароли не совпадают');
            return;
        }

        if (password.length < 4) {
            setError('Пароль должен быть не менее 4 символов');
            return;
        }

        // Проверка email
        if (!email || !validateEmail(email)) {
            setError('Введите корректный email (например: user@mail.ru)');
            return;
        }

        if (phone && !validatePhone(phone)) {
            setError('Введите корректный номер телефона (например: +79161234567 или 89161234567)');
            return;
        }

        setLoading(true);

        try {
            const response = await api.post('/auth/register', { login, password, email, phone });
            if (response.data.success) {
                setSuccess(response.data.message);
                // Очищаем форму
                setLogin('');
                setPassword('');
                setConfirmPassword('');
                setEmail('');
                setPhone('');
            } else {
                setError(response.data.message);
            }
        } catch (error) {
            setError(error.response?.data?.message || 'Ошибка регистрации');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px' }}>
            <h1 style={{ textAlign: 'center', marginBottom: '30px' }}>Регистрация</h1>

            {error && (
                <div style={{ backgroundColor: '#ffebee', color: '#c62828', padding: '10px', borderRadius: '4px', marginBottom: '20px' }}>
                    {error}
                </div>
            )}

            {success && (
                <div style={{ backgroundColor: '#d4edda', color: '#155724', padding: '15px', borderRadius: '8px', marginBottom: '20px', textAlign: 'center' }}>
                    <strong>✅ {success}</strong>
                    <p style={{ marginTop: '10px', fontSize: '14px' }}>
                        После подтверждения вы сможете войти в систему.
                    </p>
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Логин *</label>
                    <input
                        type="text"
                        value={login}
                        onChange={(e) => setLogin(e.target.value)}
                        required
                        style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                    />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Пароль *</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                    />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Подтверждение пароля *</label>
                    <input
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                        style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                    />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Email *</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                    />
                </div>

                <div style={{ marginBottom: '20px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Телефон</label>
                    <input
                        type="tel"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                    />
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    style={{
                        width: '100%',
                        padding: '12px',
                        backgroundColor: '#e67e22',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontSize: '16px',
                        cursor: 'pointer'
                    }}
                >
                    {loading ? 'Регистрация...' : 'Зарегистрироваться'}
                </button>
            </form>

            <p style={{ textAlign: 'center', marginTop: '20px' }}>
                Уже есть аккаунт? <Link to="/login">Войти</Link>
            </p>
        </div>
    );
}

export default Register;
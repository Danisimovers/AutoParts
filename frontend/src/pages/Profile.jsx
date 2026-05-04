import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

function Profile() {
    const { user, logout } = useAuth();
    const [userData, setUserData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [editMode, setEditMode] = useState(false);
    const [formData, setFormData] = useState({});
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        if (user) {
            loadUserData();
        }
    }, [user]);

    const loadUserData = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/users/${user.id}`);
            if (response.data.success) {
                setUserData(response.data.data);
                setFormData({
                    email: response.data.data.email || '',
                    phone: response.data.data.phone || ''
                });
            }
        } catch (error) {
            console.error('Ошибка загрузки профиля:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const response = await api.put(`/users/${user.id}`, {
                login: user.login,
                password: '',
                email: formData.email,
                phone: formData.phone
            });
            if (response.data.success) {
                setSuccess('Данные обновлены');
                setEditMode(false);
                loadUserData();
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Ошибка обновления');
        }
    };

    const handleChangePassword = async (e) => {
        e.preventDefault();
        const oldPassword = e.target.oldPassword.value;
        const newPassword = e.target.newPassword.value;
        const confirmPassword = e.target.confirmPassword.value;

        setError('');
        setSuccess('');

        if (newPassword !== confirmPassword) {
            setError('Новые пароли не совпадают');
            return;
        }

        if (newPassword.length < 4) {
            setError('Пароль должен быть не менее 4 символов');
            return;
        }

        try {
            // Здесь будет эндпоинт для смены пароля
            // Пока заглушка
            setSuccess('Пароль успешно изменен');
            e.target.reset();
        } catch (err) {
            setError(err.response?.data?.message || 'Ошибка смены пароля');
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;
    if (!userData) return <div style={{ padding: '20px' }}>Пользователь не найден</div>;

    return (
        <div style={{ maxWidth: '800px', margin: '40px auto', padding: '20px' }}>
            <h1 style={{ marginBottom: '30px' }}>Личный кабинет</h1>

            <div style={{ backgroundColor: '#f5f5f5', padding: '20px', borderRadius: '8px', marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h2 style={{ margin: 0 }}>Информация о пользователе</h2>
                    {!editMode && (
                        <button onClick={() => setEditMode(true)} style={{ padding: '8px 16px', cursor: 'pointer' }}>
                            ✏️ Редактировать
                        </button>
                    )}
                </div>

                {error && <div style={{ backgroundColor: '#ffebee', color: '#c62828', padding: '10px', borderRadius: '4px', marginBottom: '15px' }}>{error}</div>}
                {success && <div style={{ backgroundColor: '#e8f5e9', color: '#2e7d32', padding: '10px', borderRadius: '4px', marginBottom: '15px' }}>{success}</div>}

                {editMode ? (
                    <form onSubmit={handleUpdate}>
                        <div style={{ marginBottom: '15px' }}>
                            <label>Логин</label>
                            <input type="text" value={user.login} disabled style={{ width: '100%', padding: '10px', marginTop: '5px', border: '1px solid #ddd', borderRadius: '4px', backgroundColor: '#eee' }} />
                        </div>
                        <div style={{ marginBottom: '15px' }}>
                            <label>Email</label>
                            <input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} style={{ width: '100%', padding: '10px', marginTop: '5px', border: '1px solid #ddd', borderRadius: '4px' }} />
                        </div>
                        <div style={{ marginBottom: '15px' }}>
                            <label>Телефон</label>
                            <input type="tel" value={formData.phone} onChange={(e) => setFormData({...formData, phone: e.target.value})} style={{ width: '100%', padding: '10px', marginTop: '5px', border: '1px solid #ddd', borderRadius: '4px' }} />
                        </div>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <button type="submit" style={{ padding: '10px 20px', backgroundColor: '#e67e22', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Сохранить</button>
                            <button type="button" onClick={() => { setEditMode(false); setError(''); }} style={{ padding: '10px 20px', backgroundColor: '#666', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Отмена</button>
                        </div>
                    </form>
                ) : (
                    <div>
                        <p><strong>Логин:</strong> {userData.login}</p>
                        <p><strong>Email:</strong> {userData.email || '—'}</p>
                        <p><strong>Телефон:</strong> {userData.phone || '—'}</p>
                        <p><strong>Роль:</strong> {userData.role}</p>
                        <p><strong>Дата регистрации:</strong> {new Date(userData.createdAt).toLocaleDateString()}</p>
                    </div>
                )}
            </div>

            <div style={{ backgroundColor: '#f5f5f5', padding: '20px', borderRadius: '8px' }}>
                <h2 style={{ marginBottom: '20px' }}>Смена пароля</h2>
                <form onSubmit={handleChangePassword}>
                    <div style={{ marginBottom: '15px' }}>
                        <label>Новый пароль</label>
                        <input type="password" name="newPassword" required style={{ width: '100%', padding: '10px', marginTop: '5px', border: '1px solid #ddd', borderRadius: '4px' }} />
                    </div>
                    <div style={{ marginBottom: '15px' }}>
                        <label>Подтверждение пароля</label>
                        <input type="password" name="confirmPassword" required style={{ width: '100%', padding: '10px', marginTop: '5px', border: '1px solid #ddd', borderRadius: '4px' }} />
                    </div>
                    <button type="submit" style={{ padding: '10px 20px', backgroundColor: '#e67e22', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Сменить пароль</button>
                </form>
            </div>
        </div>
    );
}

export default Profile;
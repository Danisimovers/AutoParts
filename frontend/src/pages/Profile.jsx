import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

function Profile() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
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

    const handleChangePassword = () => {
        navigate('/forgot-password');
    };

    if (loading) return (
        <div className="flex justify-center items-center h-64">
            <div className="text-gray-500">Загрузка...</div>
        </div>
    );

    if (!userData) return (
        <div className="text-center text-red-500 py-10">Пользователь не найден</div>
    );

    return (
        <div className="max-w-3xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-8">Личный кабинет</h1>

            {/* Основная карточка */}
            <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
                {/* Шапка карточки */}
                <div className="bg-gradient-to-r from-orange-500 to-orange-600 px-6 py-4">
                    <div className="flex justify-between items-center">
                        <div className="flex items-center space-x-4">
                            <div className="bg-white rounded-full w-16 h-16 flex items-center justify-center">
                                <span className="text-2xl font-bold text-orange-500">
                                    {user?.login?.charAt(0).toUpperCase()}
                                </span>
                            </div>
                            <div>
                                <h2 className="text-white text-xl font-semibold">{userData.login}</h2>
                                <p className="text-orange-100 text-sm">Роль: {userData.role === 'ADMIN' ? 'Администратор' : userData.role === 'MANAGER' ? 'Менеджер' : 'Покупатель'}</p>
                            </div>
                        </div>
                        {!editMode && (
                            <button
                                onClick={() => setEditMode(true)}
                                className="bg-white text-orange-500 px-4 py-2 rounded-lg font-medium hover:bg-orange-50 transition"
                            >
                                Редактировать
                            </button>
                        )}
                    </div>
                </div>

                {/* Тело карточки */}
                <div className="p-6">
                    {error && (
                        <div className="bg-red-50 text-red-600 p-3 rounded-lg mb-4">
                            {error}
                        </div>
                    )}
                    {success && (
                        <div className="bg-green-50 text-green-600 p-3 rounded-lg mb-4">
                            {success}
                        </div>
                    )}

                    {editMode ? (
                        <form onSubmit={handleUpdate} className="space-y-4">
                            <div>
                                <label className="block text-gray-700 font-medium mb-1">Логин</label>
                                <input
                                    type="text"
                                    value={user.login}
                                    disabled
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-100 text-gray-500"
                                />
                            </div>
                            <div>
                                <label className="block text-gray-700 font-medium mb-1">Email</label>
                                <input
                                    type="email"
                                    value={formData.email}
                                    onChange={(e) => setFormData({...formData, email: e.target.value})}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                                    placeholder="your@email.com"
                                />
                            </div>
                            <div>
                                <label className="block text-gray-700 font-medium mb-1">Телефон</label>
                                <input
                                    type="tel"
                                    value={formData.phone}
                                    onChange={(e) => setFormData({...formData, phone: e.target.value})}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                                    placeholder="+7 (XXX) XXX-XX-XX"
                                />
                            </div>
                            <div className="flex gap-3 pt-2">
                                <button
                                    type="submit"
                                    className="bg-orange-500 text-white px-6 py-2 rounded-lg font-medium hover:bg-orange-600 transition"
                                >
                                    Сохранить
                                </button>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setEditMode(false);
                                        setError('');
                                    }}
                                    className="bg-gray-200 text-gray-700 px-6 py-2 rounded-lg font-medium hover:bg-gray-300 transition"
                                >
                                    Отмена
                                </button>
                            </div>
                        </form>
                    ) : (
                        <div className="space-y-3">
                            <div className="flex border-b pb-3">
                                <span className="w-32 text-gray-500 font-medium">Логин:</span>
                                <span className="text-gray-800">{userData.login}</span>
                            </div>
                            <div className="flex border-b pb-3">
                                <span className="w-32 text-gray-500 font-medium">Email:</span>
                                <span className="text-gray-800">{userData.email || '—'}</span>
                            </div>
                            <div className="flex border-b pb-3">
                                <span className="w-32 text-gray-500 font-medium">Телефон:</span>
                                <span className="text-gray-800">{userData.phone || '—'}</span>
                            </div>
                            <div className="flex border-b pb-3">
                                <span className="w-32 text-gray-500 font-medium">Роль:</span>
                                <span className="text-gray-800">
                                    {userData.role === 'ADMIN' ? 'Администратор' :
                                        userData.role === 'MANAGER' ? 'Менеджер' : 'Покупатель'}
                                </span>
                            </div>
                            <div className="flex pb-2">
                                <span className="w-32 text-gray-500 font-medium">Регистрация:</span>
                                <span className="text-gray-800">{new Date(userData.createdAt).toLocaleDateString()}</span>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Карточка смены пароля */}
            <div className="mt-6 bg-white rounded-2xl shadow-lg p-6">
                <h3 className="text-lg font-semibold text-gray-800 mb-2">Безопасность</h3>
                <p className="text-gray-500 text-sm mb-4">
                    Хотите изменить пароль? Вы можете запросить сброс пароля на вашу электронную почту.
                </p>
                <button
                    onClick={handleChangePassword}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg font-medium hover:bg-orange-600 transition"
                >
                    Изменить пароль
                </button>
            </div>
        </div>
    );
}

export default Profile;
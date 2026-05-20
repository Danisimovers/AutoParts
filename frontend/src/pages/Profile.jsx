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
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deletePassword, setDeletePassword] = useState('');
    const [deleteError, setDeleteError] = useState('');
    const [deleting, setDeleting] = useState(false);

    // Состояния для модального окна подтверждения пароля
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [currentPassword, setCurrentPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [verifying, setVerifying] = useState(false);

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

    // Проверка текущего пароля перед сменой
    const handleVerifyPassword = async (e) => {
        e.preventDefault();
        setPasswordError('');

        if (!currentPassword.trim()) {
            setPasswordError('Введите текущий пароль');
            return;
        }

        setVerifying(true);

        try {
            const response = await api.post('/auth/login', {
                login: user.login,
                password: currentPassword
            });

            if (response.data.success) {
                setShowPasswordModal(false);
                setCurrentPassword('');
                navigate('/forgot-password');
            } else {
                setPasswordError('Неверный пароль');
            }
        } catch (error) {
            if (error.response?.status === 401) {
                setPasswordError('Неверный пароль');
            } else {
                setPasswordError('Ошибка проверки пароля. Попробуйте позже.');
            }
        } finally {
            setVerifying(false);
        }
    };

    const handleChangePassword = () => {
        // Открываем модальное окно для ввода текущего пароля
        setShowPasswordModal(true);
        setCurrentPassword('');
        setPasswordError('');
    };

    const handleDeleteAccount = async () => {
        if (!deletePassword.trim()) {
            setDeleteError('Введите пароль для подтверждения');
            return;
        }

        setDeleting(true);
        setDeleteError('');

        try {
            const loginResponse = await api.post('/auth/login', {
                login: user.login,
                password: deletePassword
            });

            if (!loginResponse.data.success) {
                setDeleteError('Неверный пароль');
                setDeleting(false);
                return;
            }

            await api.delete('/users/me');
            alert('Аккаунт успешно удален');
            setShowDeleteModal(false);
            setDeletePassword('');
            logout();
            navigate('/');
        } catch (error) {
            setDeleteError(error.response?.data?.message || 'Ошибка при удалении аккаунта');
            setDeleting(false);
        }
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
                                <p className="text-orange-100 text-sm">
                                    Роль: {userData.role === 'ADMIN' ? 'Администратор' : userData.role === 'MANAGER' ? 'Менеджер' : 'Покупатель'}
                                </p>
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
                    Хотите изменить пароль? Сначала подтвердите текущий пароль.
                </p>
                <button
                    onClick={handleChangePassword}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg font-medium hover:bg-orange-600 transition"
                >
                    Изменить пароль
                </button>
            </div>

            {/* Карточка удаления аккаунта */}
            <div className="mt-6 bg-white rounded-2xl shadow-lg p-6 border border-red-200">
                <h3 className="text-lg font-semibold text-red-600 mb-2">Опасная зона</h3>
                <p className="text-gray-500 text-sm mb-4">
                    Удаление аккаунта приведет к безвозвратной потере всех данных: история заказов, возвраты, заявки.
                    Это действие невозможно отменить.
                </p>
                <button
                    onClick={() => setShowDeleteModal(true)}
                    className="bg-red-500 text-white px-6 py-2 rounded-lg font-medium hover:bg-red-600 transition"
                >
                    Удалить аккаунт
                </button>
            </div>

            {/* Модальное окно подтверждения пароля для смены */}
            {showPasswordModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-2xl max-w-md w-full mx-4 p-6">
                        <h3 className="text-xl font-bold text-gray-800 mb-4">Подтверждение пароля</h3>
                        <p className="text-gray-600 mb-4">
                            Для смены пароля подтвердите ваш текущий пароль.
                        </p>
                        <form onSubmit={handleVerifyPassword}>
                            <div className="mb-4">
                                <label className="block text-gray-700 font-medium mb-1">Текущий пароль</label>
                                <input
                                    type="password"
                                    value={currentPassword}
                                    onChange={(e) => setCurrentPassword(e.target.value)}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                                    placeholder="Введите текущий пароль"
                                    autoFocus
                                />
                            </div>
                            {passwordError && (
                                <div className="bg-red-50 text-red-600 p-2 rounded-lg mb-4 text-sm">
                                    {passwordError}
                                </div>
                            )}
                            <div className="flex gap-3 justify-end">
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowPasswordModal(false);
                                        setCurrentPassword('');
                                        setPasswordError('');
                                    }}
                                    className="bg-gray-200 text-gray-700 px-4 py-2 rounded-lg font-medium hover:bg-gray-300 transition"
                                >
                                    Отмена
                                </button>
                                <button
                                    type="submit"
                                    disabled={verifying}
                                    className="bg-orange-500 text-white px-4 py-2 rounded-lg font-medium hover:bg-orange-600 transition disabled:opacity-50"
                                >
                                    {verifying ? 'Проверка...' : 'Подтвердить'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Модальное окно подтверждения удаления */}
            {showDeleteModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-2xl max-w-md w-full mx-4 p-6">
                        <h3 className="text-xl font-bold text-gray-800 mb-4">Удаление аккаунта</h3>
                        <p className="text-gray-600 mb-4">
                            Вы уверены, что хотите удалить свой аккаунт? Это действие необратимо.
                        </p>
                        <div className="mb-4">
                            <label className="block text-gray-700 font-medium mb-1">Введите пароль для подтверждения</label>
                            <input
                                type="password"
                                value={deletePassword}
                                onChange={(e) => setDeletePassword(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                                placeholder="Ваш пароль"
                            />
                        </div>
                        {deleteError && (
                            <div className="bg-red-50 text-red-600 p-2 rounded-lg mb-4 text-sm">
                                {deleteError}
                            </div>
                        )}
                        <div className="flex gap-3 justify-end">
                            <button
                                onClick={() => {
                                    setShowDeleteModal(false);
                                    setDeletePassword('');
                                    setDeleteError('');
                                }}
                                className="bg-gray-200 text-gray-700 px-4 py-2 rounded-lg font-medium hover:bg-gray-300 transition"
                            >
                                Отмена
                            </button>
                            <button
                                onClick={handleDeleteAccount}
                                disabled={deleting}
                                className="bg-red-500 text-white px-4 py-2 rounded-lg font-medium hover:bg-red-600 transition disabled:opacity-50"
                            >
                                {deleting ? 'Удаление...' : 'Удалить'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Profile;
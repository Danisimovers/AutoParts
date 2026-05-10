import React, { useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/api';
import { Lock, ArrowLeft, Eye, EyeOff } from 'lucide-react';

function ResetPassword() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();

    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!token) {
            setError('Неверная ссылка для сброса пароля');
            return;
        }

        if (newPassword !== confirmPassword) {
            setError('Пароли не совпадают');
            return;
        }

        if (newPassword.length < 4) {
            setError('Пароль должен быть не менее 4 символов');
            return;
        }

        setLoading(true);

        try {
            const response = await api.post('/auth/reset-password', {
                token,
                newPassword
            });
            if (response.data.success) {
                setSuccess(response.data.message);
                setTimeout(() => {
                    navigate('/login');
                }, 3000);
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Ошибка при сбросе пароля');
        } finally {
            setLoading(false);
        }
    };

    // Если нет токена
    if (!token) {
        return (
            <div className="min-h-[70vh] flex items-center justify-center p-5">
                <div className="max-w-md w-full">
                    <div className="bg-red-50 text-red-700 p-8 rounded-2xl text-center">
                        <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <Lock size={32} className="text-red-600" />
                        </div>
                        <h2 className="text-xl font-bold mb-2">Ошибка</h2>
                        <p className="mb-4">Неверная ссылка для сброса пароля</p>
                        <Link
                            to="/forgot-password"
                            className="inline-block text-orange-500 hover:text-orange-600 font-medium transition"
                        >
                            Запросить новую ссылку
                        </Link>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-[80vh] flex items-center justify-center p-5">
            <div className="max-w-md w-full">
                <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
                    <div className="text-center mb-6">
                        <div className="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <Lock size={32} className="text-orange-500" />
                        </div>
                        <h1 className="text-2xl font-bold text-gray-800">
                            Сброс пароля
                        </h1>
                        <p className="text-gray-500 text-sm mt-2">
                            Введите новый пароль для вашей учетной записи
                        </p>
                    </div>

                    {error && (
                        <div className="bg-red-50 text-red-700 p-3 rounded-xl mb-5 text-sm">
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="bg-green-50 text-green-700 p-4 rounded-xl mb-5 text-center">
                            <strong className="block mb-2">{success}</strong>
                            <p className="text-sm">Перенаправление на страницу входа...</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Новый пароль
                            </label>
                            <div className="relative">
                                <Lock size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                    className="w-full pl-10 pr-12 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                                    placeholder="Минимум 4 символа"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                >
                                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                </button>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Подтверждение пароля
                            </label>
                            <div className="relative">
                                <Lock size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    className="w-full pl-10 pr-12 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                                    placeholder="Повторите новый пароль"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                >
                                    {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                </button>
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className={`
                                w-full py-3 bg-orange-500 text-white rounded-xl
                                font-medium text-base transition-all duration-200
                                ${loading
                                ? 'opacity-50 cursor-not-allowed'
                                : 'hover:bg-orange-600 hover:shadow-md cursor-pointer'
                            }
                            `}
                        >
                            {loading ? 'Сброс...' : 'Сбросить пароль'}
                        </button>
                    </form>

                    <p className="text-center mt-6">
                        <Link to="/login" className="inline-flex items-center gap-1 text-gray-500 hover:text-orange-500 transition text-sm">
                            <ArrowLeft size={14} />
                            Вернуться ко входу
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default ResetPassword;
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';
import { User, Lock, Mail, Phone, Eye, EyeOff } from 'lucide-react';

function Register() {
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    // Валидация email
    const validateEmail = (email) => {
        const emailRegex = /^[^\s@]+@([^\s@.,]+\.)+[^\s@.,]{2,}$/;
        return emailRegex.test(email);
    };

    const validatePhone = (phone) => {
        const cleaned = phone.replace(/[^\d+]/g, '');
        const phoneRegex = /^(\+7|8)?9\d{9}$/;
        return phoneRegex.test(cleaned);
    };

    const formatPhone = (value) => {
        const cleaned = value.replace(/\D/g, '');
        if (cleaned.length === 0) return '';

        let formatted = '';
        if (cleaned.startsWith('7') || cleaned.startsWith('8')) {
            formatted = '+' + cleaned;
        } else if (cleaned.startsWith('9')) {
            formatted = '+7' + cleaned;
        } else {
            formatted = '+' + cleaned;
        }

        return formatted;
    };

    const handlePhoneChange = (e) => {
        const formatted = formatPhone(e.target.value);
        setPhone(formatted);
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

        if (!email || !validateEmail(email)) {
            setError('Введите корректный email (например: user@mail.ru)');
            return;
        }

        // Телефон стал обязательным!
        if (!phone) {
            setError('Телефон обязателен для регистрации');
            return;
        }

        if (!validatePhone(phone)) {
            setError('Введите корректный номер телефона (например: +79161234567 или 89161234567)');
            return;
        }

        setLoading(true);

        try {
            const response = await api.post('/auth/register', { login, password, email, phone });
            if (response.data.success) {
                setSuccess(response.data.message);
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
        <div className="min-h-[80vh] flex items-center justify-center p-5">
            <div className="max-w-md w-full">
                <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
                    <h1 className="text-2xl font-bold text-gray-800 text-center mb-6">
                        Регистрация
                    </h1>

                    {error && (
                        <div className="bg-red-50 text-red-700 p-3 rounded-xl mb-5 text-sm">
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="bg-green-50 text-green-700 p-4 rounded-xl mb-5 text-center">
                            <strong className="block mb-2">✅ {success}</strong>
                            <p className="text-sm">
                                После подтверждения вы сможете войти в систему.
                            </p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Логин <span className="text-red-500">*</span>
                            </label>
                            <div className="relative">
                                <User size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type="text"
                                    value={login}
                                    onChange={(e) => setLogin(e.target.value)}
                                    required
                                    className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                                    placeholder="Введите логин"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Пароль <span className="text-red-500">*</span>
                            </label>
                            <div className="relative">
                                <Lock size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
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
                                Подтверждение пароля <span className="text-red-500">*</span>
                            </label>
                            <div className="relative">
                                <Lock size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    className="w-full pl-10 pr-12 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                                    placeholder="Повторите пароль"
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

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Email <span className="text-red-500">*</span>
                            </label>
                            <div className="relative">
                                <Mail size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                                    placeholder="user@example.com"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Телефон <span className="text-red-500">*</span>
                            </label>
                            <div className="relative">
                                <Phone size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <input
                                    type="tel"
                                    value={phone}
                                    onChange={handlePhoneChange}
                                    required
                                    className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                                    placeholder="+7 (999) 123-45-67"
                                />
                            </div>
                            <p className="text-xs text-gray-400 mt-1">
                                Формат: +7XXXXXXXXXX или 8XXXXXXXXXX
                            </p>
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
                            {loading ? 'Регистрация...' : 'Зарегистрироваться'}
                        </button>
                    </form>

                    <p className="text-center mt-6 text-gray-600">
                        Уже есть аккаунт?{' '}
                        <Link to="/login" className="text-orange-500 hover:text-orange-600 font-medium transition">
                            Войти
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default Register;
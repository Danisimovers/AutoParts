import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';

function ForgotPassword() {
    const [email, setEmail] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            const response = await api.post('/auth/forgot-password', { email });
            if (response.data.success) {
                setSuccess(response.data.message);
                setEmail('');
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Ошибка при отправке запроса');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-[400px] mx-auto my-[50px] p-5">
            <h1 className="text-center mb-[30px] text-2xl font-bold text-gray-800">
                Восстановление пароля
            </h1>

            {error && (
                <div className="bg-red-50 text-red-700 p-2.5 rounded mb-5 text-sm">
                    {error}
                </div>
            )}

            {success && (
                <div className="bg-green-50 text-green-700 p-4 rounded-lg mb-5 text-center">
                    <strong className="block mb-2">{success}</strong>
                    <p className="mt-2.5 text-sm">
                        Проверьте почту и перейдите по ссылке для сброса пароля.
                    </p>
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div className="mb-5">
                    <label className="block mb-1.5 text-sm font-medium text-gray-700">
                        Email
                    </label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        placeholder="Введите email, указанный при регистрации"
                        className="w-full p-2.5 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                    />
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className={`
                        w-full py-3 bg-orange-500 text-white border-none rounded
                        text-base font-medium transition-colors duration-200
                        ${loading
                        ? 'opacity-50 cursor-not-allowed'
                        : 'hover:bg-orange-600 cursor-pointer'
                    }
                    `}
                >
                    {loading ? 'Отправка...' : 'Отправить ссылку для сброса'}
                </button>
            </form>

            <p className="text-center mt-5">
                <Link to="/login" className="text-orange-500 hover:text-orange-600 transition">
                    Вернуться ко входу
                </Link>
            </p>
        </div>
    );
}

export default ForgotPassword;
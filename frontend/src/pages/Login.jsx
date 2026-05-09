import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Login() {
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login: loginUser } = useAuth();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        const result = await loginUser(login, password);
        setLoading(false);

        if (result.success) {
            navigate('/');
        } else {
            setError(result.error);
        }
    };

    return (
        <div className="max-w-[400px] mx-auto my-[50px] p-5">
            <h1 className="text-center mb-[30px] text-2xl font-bold text-gray-800">
                Вход
            </h1>

            {error && (
                <div className="bg-red-50 text-red-700 p-2.5 rounded mb-5 text-sm">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div className="mb-4">
                    <label className="block mb-1.5 text-sm font-medium text-gray-700">
                        Логин
                    </label>
                    <input
                        type="text"
                        value={login}
                        onChange={(e) => setLogin(e.target.value)}
                        required
                        className="w-full p-2.5 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition"
                    />
                </div>

                <div className="mb-5">
                    <label className="block mb-1.5 text-sm font-medium text-gray-700">
                        Пароль
                    </label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
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
                    {loading ? 'Вход...' : 'Войти'}
                </button>
            </form>

            <p className="text-center mt-4">
                <Link to="/forgot-password" className="text-orange-500 hover:text-orange-600 transition">
                    Забыли пароль?
                </Link>
            </p>

            <p className="text-center mt-4 text-gray-600">
                Нет аккаунта?{' '}
                <Link to="/register" className="text-orange-500 hover:text-orange-600 transition">
                    Зарегистрироваться
                </Link>
            </p>
        </div>
    );
}

export default Login;
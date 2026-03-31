import React, { createContext, useState, useContext, useEffect } from 'react';
import api from '../api/api';

const AuthContext = createContext();

export function useAuth() {
    return useContext(AuthContext);
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        const storedUser = localStorage.getItem('user');

        if (token && storedUser) {
            setUser(JSON.parse(storedUser));
        }
        setLoading(false);
    }, []);

    const login = async (login, password) => {
        try {
            const response = await api.post('/auth/login', { login, password });
            if (response.data.success) {
                const { token, userId, login: userLogin, email, phone, role } = response.data.data;
                const userData = { id: userId, login: userLogin, email, phone, role };
                setUser(userData);
                localStorage.setItem('token', token);
                localStorage.setItem('user', JSON.stringify(userData));
                return { success: true, user: userData };
            }
            return { success: false, error: response.data.message };
        } catch (error) {
            console.error('Ошибка входа:', error);
            return { success: false, error: error.response?.data?.message || 'Ошибка входа' };
        }
    };

    const register = async (login, password, email, phone) => {
        try {
            const response = await api.post('/auth/register', { login, password, email, phone });
            if (response.data.success) {
                const { token, userId, login: userLogin, email: userEmail, phone: userPhone, role } = response.data.data;
                const userData = { id: userId, login: userLogin, email: userEmail, phone: userPhone, role };
                setUser(userData);
                localStorage.setItem('token', token);
                localStorage.setItem('user', JSON.stringify(userData));
                return { success: true, user: userData };
            }
            return { success: false, error: response.data.message };
        } catch (error) {
            console.error('Ошибка регистрации:', error);
            return { success: false, error: error.response?.data?.message || 'Ошибка регистрации' };
        }
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    };

    const value = {
        user,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        isAdmin: user?.role === 'ADMIN'
    };

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    );
}
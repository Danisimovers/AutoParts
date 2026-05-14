import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8081/api',
    headers: {
        'Content-Type': 'application/json',
    }
});

// Перехватчик: добавляем токен в каждый запрос
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        console.log('[API] Token from localStorage:', token ? token.substring(0, 50) + '...' : 'null');

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log('[API] Authorization header set:', config.headers.Authorization.substring(0, 60) + '...');
        } else {
            console.log('[API] No token found');
        }

        console.log('[API] Request URL:', config.baseURL + config.url);
        console.log('[API] Request method:', config.method);

        return config;
    },
    (error) => {
        console.error('[API] Request error:', error);
        return Promise.reject(error);
    }
);

// Перехватчик для ответов
api.interceptors.response.use(
    (response) => {
        console.log('[API] Response status:', response.status);
        return response;
    },
    (error) => {
        console.error('[API] Response error:', error.response?.status, error.response?.data);
        return Promise.reject(error);
    }
);

export default api;
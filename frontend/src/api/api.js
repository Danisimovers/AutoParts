import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8081/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,  // <-- ЭТО ВАЖНО! Отправляет cookies (сессию)
});

export default api;
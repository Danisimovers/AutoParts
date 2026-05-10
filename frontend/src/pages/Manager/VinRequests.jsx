import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/api';

function VinRequests() {
    const navigate = useNavigate();
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadRequests();
    }, []);

    const loadRequests = async () => {
        setLoading(true);
        try {
            const response = await api.get('/manager/vin-requests');
            if (response.data.success) {
                setRequests(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки заявок:', error);
            alert('Ошибка загрузки заявок');
        } finally {
            setLoading(false);
        }
    };

    const updateStatus = async (id, status) => {
        try {
            await api.put(`/manager/vin-requests/${id}/status?status=${status}`);
            loadRequests();
            alert('Статус обновлен');
        } catch (error) {
            alert('Ошибка обновления статуса');
        }
    };

    const getStatusClass = (status) => {
        switch(status) {
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'PROCESSING': return 'bg-blue-100 text-blue-800';
            case 'COMPLETED': return 'bg-green-100 text-green-800';
            case 'REJECTED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'PROCESSING': return 'В обработке';
            case 'COMPLETED': return 'Выполнена';
            case 'REJECTED': return 'Отклонена';
            default: return status;
        }
    };

    if (loading) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Заявки по VIN</h1>
                <p className="text-sm text-gray-500 mt-1">Всего заявок: {requests.length}</p>
            </div>

            <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                <table className="w-full">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID пользователя</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">VIN</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Описание</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Статус</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {requests.map(req => (
                        <tr key={req.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{req.id}</td>
                            <td className="p-3 text-sm text-gray-600">{req.userId}</td>
                            <td className="p-3 text-sm font-mono text-gray-700">{req.vin}</td>
                            <td className="p-3 text-sm text-gray-600 max-w-xs truncate">
                                {req.description || '-'}
                            </td>
                            <td className="text-center p-3">
                                    <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusClass(req.status)}`}>
                                        {getStatusText(req.status)}
                                    </span>
                            </td>
                            <td className="text-center p-3">
                                <div className="flex gap-2 justify-center">
                                    <select
                                        onChange={(e) => updateStatus(req.id, e.target.value)}
                                        defaultValue={req.status}
                                        className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                    >
                                        <option value="PENDING">Ожидает</option>
                                        <option value="PROCESSING">В обработке</option>
                                        <option value="COMPLETED">Выполнена</option>
                                        <option value="REJECTED">Отклонена</option>
                                    </select>
                                    <button
                                        onClick={() => navigate(`/manager/vin-requests/${req.id}`)}
                                        className="bg-orange-500 text-white px-3 py-1.5 rounded-lg hover:bg-orange-600 transition text-sm"
                                    >
                                        Ответить
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default VinRequests;
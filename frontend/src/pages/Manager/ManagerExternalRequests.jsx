import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function ManagerExternalRequests() {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadRequests();
    }, []);

    const loadRequests = async () => {
        setLoading(true);
        try {
            const response = await api.get('/manager/external-requests');
            if (response.data.success) {
                setRequests(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки запросов:', error);
            alert('Ошибка загрузки запросов');
        } finally {
            setLoading(false);
        }
    };

    const updateStatus = async (id, status) => {
        try {
            await api.put(`/manager/external-requests/${id}/status?status=${status}`);
            alert('Статус обновлен');
            loadRequests();
        } catch (error) {
            alert('Ошибка обновления статуса');
        }
    };

    const getStatusClass = (status) => {
        switch(status) {
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'PROCESSING': return 'bg-blue-100 text-blue-800';
            case 'ORDERED': return 'bg-purple-100 text-purple-800';
            case 'COMPLETED': return 'bg-green-100 text-green-800';
            case 'REJECTED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'PROCESSING': return 'В обработке';
            case 'ORDERED': return 'Заказан';
            case 'COMPLETED': return 'Выполнен';
            case 'REJECTED': return 'Отклонен';
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
                <h1 className="text-2xl font-bold text-gray-800">Запросы товаров от поставщиков</h1>
                <p className="text-sm text-gray-500 mt-1">Всего запросов: {requests.length}</p>
            </div>

            {requests.length === 0 ? (
                <div className="text-center text-gray-400 py-10 bg-white rounded-xl border border-gray-200">
                    Нет запросов
                </div>
            ) : (
                <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                    <table className="w-full">
                        <thead>
                        <tr className="bg-gray-50 border-b border-gray-200">
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">Товар</th>
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">Артикул</th>
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">Производитель</th>
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">Поставщик</th>
                            <th className="text-center p-3 text-sm font-semibold text-gray-600">Цена</th>
                            <th className="text-center p-3 text-sm font-semibold text-gray-600">Статус</th>
                            <th className="text-center p-3 text-sm font-semibold text-gray-600">Действие</th>
                        </tr>
                        </thead>
                        <tbody>
                        {requests.map(req => (
                            <tr key={req.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                                <td className="p-3 text-sm text-gray-500">{req.id}</td>
                                <td className="p-3 text-sm font-medium text-gray-800">{req.productName}</td>
                                <td className="p-3 text-sm font-mono text-gray-600">{req.factoryNumber}</td>
                                <td className="p-3 text-sm text-gray-600">{req.producer || '-'}</td>
                                <td className="p-3 text-sm text-gray-600">{req.supplierName}</td>
                                <td className="text-center p-3 text-sm font-semibold text-gray-800">{req.price} ₽</td>
                                <td className="text-center p-3">
                                        <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusClass(req.status)}`}>
                                            {getStatusText(req.status)}
                                        </span>
                                </td>
                                <td className="text-center p-3">
                                    <select
                                        onChange={(e) => updateStatus(req.id, e.target.value)}
                                        defaultValue={req.status}
                                        className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                    >
                                        <option value="PENDING">Ожидает</option>
                                        <option value="PROCESSING">В обработке</option>
                                        <option value="ORDERED">Заказан</option>
                                        <option value="COMPLETED">Выполнен</option>
                                        <option value="REJECTED">Отклонен</option>
                                    </select>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default ManagerExternalRequests;
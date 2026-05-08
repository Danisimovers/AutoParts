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

    const getStatusColor = (status) => {
        switch(status) {
            case 'PENDING': return 'bg-yellow-500';
            case 'PROCESSING': return 'bg-blue-500';
            case 'ORDERED': return 'bg-purple-500';
            case 'COMPLETED': return 'bg-green-500';
            case 'REJECTED': return 'bg-red-500';
            default: return 'bg-gray-500';
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

    if (loading) return <div className="p-5">Загрузка...</div>;

    return (
        <div className="p-5">
            <h1 className="text-3xl font-bold mb-6">Запросы товаров от поставщиков</h1>

            {requests.length === 0 ? (
                <div className="text-center text-gray-500 py-10">Нет запросов</div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="w-full border-collapse">
                        <thead className="bg-gray-100">
                        <tr>
                            <th className="p-2 text-left">ID</th>
                            <th className="p-2 text-left">Товар</th>
                            <th className="p-2 text-left">Артикул</th>
                            <th className="p-2 text-left">Производитель</th>
                            <th className="p-2 text-left">Поставщик</th>
                            <th className="p-2 text-center">Цена</th>
                            <th className="p-2 text-center">Статус</th>
                            <th className="p-2 text-center">Действие</th>
                        </tr>
                        </thead>
                        <tbody>
                        {requests.map(req => (
                            <tr key={req.id} className="border-b">
                                <td className="p-2">{req.id}</td>
                                <td className="p-2">{req.productName}</td>
                                <td className="p-2">{req.factoryNumber}</td>
                                <td className="p-2">{req.producer || '-'}</td>
                                <td className="p-2">{req.supplierName}</td>
                                <td className="p-2 text-center">{req.price} ₽</td>
                                <td className="p-2 text-center">
                                        <span className={`${getStatusColor(req.status)} text-white px-2 py-1 rounded-full text-xs`}>
                                            {getStatusText(req.status)}
                                        </span>
                                </td>
                                <td className="p-2 text-center">
                                    <select
                                        onChange={(e) => updateStatus(req.id, e.target.value)}
                                        defaultValue={req.status}
                                        className="p-1 border rounded"
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
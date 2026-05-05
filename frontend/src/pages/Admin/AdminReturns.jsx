import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminReturns() {
    const [returns, setReturns] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadReturns();
    }, []);

    const loadReturns = async () => {
        setLoading(true);
        try {
            const response = await api.get('/returns');
            if (response.data.success) {
                setReturns(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки заявок:', error);
            alert('Ошибка загрузки заявок');
        } finally {
            setLoading(false);
        }
    };

    const getStatusColor = (status) => {
        switch(status) {
            case 'PENDING': return 'bg-yellow-500';
            case 'APPROVED': return 'bg-green-500';
            case 'REJECTED': return 'bg-red-500';
            default: return 'bg-gray-500';
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'APPROVED': return 'Одобрен';
            case 'REJECTED': return 'Отклонен';
            default: return status;
        }
    };

    if (loading) return <div className="p-5">Загрузка...</div>;

    return (
        <div className="p-5">
            <h1 className="text-3xl font-bold mb-6">Заявки на возврат</h1>

            {returns.length === 0 ? (
                <div className="text-center text-gray-500 py-10">Нет заявок на возврат</div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="w-full border-collapse">
                        <thead>
                        <tr className="border-b border-gray-300">
                            <th className="text-left p-2">ID</th>
                            <th className="text-left p-2">ID заказа</th>
                            <th className="text-left p-2">ID пользователя</th>
                            <th className="text-left p-2">Причина</th>
                            <th className="text-center p-2">Статус</th>
                            <th className="text-center p-2">Дата</th>
                        </tr>
                        </thead>
                        <tbody>
                        {returns.map(ret => (
                            <tr key={ret.id} className="border-b border-gray-200">
                                <td className="p-2">{ret.id}</td>
                                <td className="p-2">{ret.orderItemId}</td>
                                <td className="p-2">{ret.userId}</td>
                                <td className="p-2 max-w-xs">{ret.reason}</td>
                                <td className="text-center p-2">
                                        <span className={`${getStatusColor(ret.status)} text-white px-2 py-1 rounded-full text-xs`}>
                                            {getStatusText(ret.status)}
                                        </span>
                                </td>
                                <td className="text-center p-2">
                                    {new Date(ret.createdAt).toLocaleDateString()}
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

export default AdminReturns;
import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function ManagerReturns() {
    const [returns, setReturns] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedReturn, setSelectedReturn] = useState(null);
    const [rejectReason, setRejectReason] = useState('');
    const [showRejectModal, setShowRejectModal] = useState(false);

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

    const approveReturn = async (id) => {
        if (!window.confirm('Одобрить возврат?')) return;
        try {
            await api.put(`/returns/${id}/approve`);
            alert('Заявка одобрена');
            loadReturns();
        } catch (error) {
            alert('Ошибка одобрения');
        }
    };

    const rejectReturn = async () => {
        if (!rejectReason.trim()) {
            alert('Укажите причину отклонения');
            return;
        }
        try {
            await api.put(`/returns/${selectedReturn.id}/reject`, { reason: rejectReason });
            alert('Заявка отклонена');
            setShowRejectModal(false);
            setRejectReason('');
            loadReturns();
        } catch (error) {
            alert('Ошибка отклонения');
        }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'APPROVED': return 'bg-green-100 text-green-800';
            case 'REJECTED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'PENDING': return 'Ожидает';
            case 'APPROVED': return 'Одобрен';
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
                <h1 className="text-2xl font-bold text-gray-800">Заявки на возврат</h1>
                <p className="text-sm text-gray-500 mt-1">Всего заявок: {returns.length}</p>
            </div>

            {returns.length === 0 ? (
                <div className="text-center text-gray-400 py-10 bg-white rounded-xl border border-gray-200">
                    Нет заявок на возврат
                </div>
            ) : (
                <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                    <table className="w-full">
                        <thead>
                        <tr className="bg-gray-50 border-b border-gray-200">
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">ID заказа</th>
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">ID пользователя</th>
                            <th className="text-left p-3 text-sm font-semibold text-gray-600">Причина</th>
                            <th className="text-center p-3 text-sm font-semibold text-gray-600">Статус</th>
                            <th className="text-center p-3 text-sm font-semibold text-gray-600">Дата</th>
                            <th className="text-center p-3 text-sm font-semibold text-gray-600">Действия</th>
                        </tr>
                        </thead>
                        <tbody>
                        {returns.map((ret) => (
                            <tr key={ret.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                                <td className="p-3 text-sm text-gray-500">{ret.id}</td>
                                <td className="p-3 text-sm font-medium text-gray-800">#{ret.orderId}</td>
                                <td className="p-3 text-sm text-gray-600">{ret.userId}</td>
                                <td className="p-3 text-sm text-gray-600 max-w-xs truncate">{ret.reason}</td>
                                <td className="text-center p-3">
                                        <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusClass(ret.status)}`}>
                                            {getStatusText(ret.status)}
                                        </span>
                                </td>
                                <td className="text-center p-3 text-sm text-gray-600">
                                    {new Date(ret.createdAt).toLocaleDateString()}
                                </td>
                                <td className="text-center p-3">
                                    {ret.status === 'PENDING' && (
                                        <div className="flex gap-2 justify-center">
                                            <button
                                                onClick={() => approveReturn(ret.id)}
                                                className="bg-green-500 text-white px-3 py-1 rounded-md hover:bg-green-600 transition text-sm"
                                            >
                                                Одобрить
                                            </button>
                                            <button
                                                onClick={() => {
                                                    setSelectedReturn(ret);
                                                    setShowRejectModal(true);
                                                }}
                                                className="bg-red-500 text-white px-3 py-1 rounded-md hover:bg-red-600 transition text-sm"
                                            >
                                                Отклонить
                                            </button>
                                        </div>
                                    )}
                                    {ret.status !== 'PENDING' && (
                                        <span className="text-gray-400 text-sm">Обработана</span>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Модальное окно для отклонения */}
            {showRejectModal && selectedReturn && (
                <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-[1000]">
                    <div className="bg-white rounded-xl p-6 max-w-md w-[90%] shadow-xl">
                        <h2 className="text-xl font-bold text-gray-800 mb-4">Отклонение возврата</h2>
                        <p className="text-gray-600 mb-4">
                            Заявка #{selectedReturn.id} от пользователя ID: {selectedReturn.userId}
                        </p>
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Причина отклонения *
                            </label>
                            <textarea
                                value={rejectReason}
                                onChange={(e) => setRejectReason(e.target.value)}
                                rows="3"
                                placeholder="Укажите причину отклонения..."
                                className="w-full p-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 resize-y"
                            />
                        </div>
                        <div className="flex gap-3 justify-end">
                            <button
                                onClick={() => {
                                    setShowRejectModal(false);
                                    setRejectReason('');
                                }}
                                className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-600 transition"
                            >
                                Отмена
                            </button>
                            <button
                                onClick={rejectReturn}
                                className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition"
                            >
                                Отклонить
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ManagerReturns;
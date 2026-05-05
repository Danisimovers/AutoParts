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

    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING':
                return 'bg-yellow-500';
            case 'APPROVED':
                return 'bg-green-500';
            case 'REJECTED':
                return 'bg-red-500';
            default:
                return 'bg-gray-500';
        }
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'PENDING':
                return 'Ожидает';
            case 'APPROVED':
                return 'Одобрен';
            case 'REJECTED':
                return 'Отклонен';
            default:
                return status;
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
                        <thead className="border-b border-gray-300">
                        <tr>
                            <th className="text-left p-2">ID</th>
                            <th className="text-left p-2">ID заказа</th>
                            <th className="text-left p-2">ID пользователя</th>
                            <th className="text-left p-2">Причина</th>
                            <th className="text-center p-2">Статус</th>
                            <th className="text-center p-2">Дата</th>
                            <th className="text-center p-2">Действия</th>
                        </tr>
                        </thead>
                        <tbody>
                        {returns.map((ret) => (
                            <tr key={ret.id} className="border-b border-gray-200">
                                <td className="p-2">{ret.id}</td>
                                <td className="p-2">{ret.orderId}</td>
                                <td className="p-2">{ret.userId}</td>
                                <td className="p-2 max-w-xs">{ret.reason}</td>
                                <td className="text-center p-2">
                                        <span
                                            className={`${getStatusColor(ret.status)} text-white px-2 py-1 rounded-full text-xs`}
                                        >
                                            {getStatusText(ret.status)}
                                        </span>
                                </td>
                                <td className="text-center p-2">
                                    {new Date(ret.createdAt).toLocaleDateString()}
                                </td>
                                <td className="text-center p-2">
                                    {ret.status === 'PENDING' && (
                                        <div className="flex gap-2 justify-center">
                                            <button
                                                onClick={() => approveReturn(ret.id)}
                                                className="bg-green-500 text-white px-3 py-1 rounded cursor-pointer hover:bg-green-600"
                                            >
                                                Одобрить
                                            </button>
                                            <button
                                                onClick={() => {
                                                    setSelectedReturn(ret);
                                                    setShowRejectModal(true);
                                                }}
                                                className="bg-red-500 text-white px-3 py-1 rounded cursor-pointer hover:bg-red-600"
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
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-[1000]">
                    <div className="bg-white p-6 rounded-lg max-w-md w-[90%]">
                        <h2 className="text-xl font-bold mb-4">Отклонение возврата</h2>
                        <p className="mb-4">
                            Заявка #{selectedReturn.id} от пользователя ID:{selectedReturn.userId}
                        </p>
                        <div className="mb-4">
                            <label className="block mb-1">Причина отклонения *</label>
                            <textarea
                                value={rejectReason}
                                onChange={(e) => setRejectReason(e.target.value)}
                                rows="3"
                                placeholder="Укажите причину отклонения..."
                                className="w-full p-2 border border-gray-300 rounded resize-y"
                            />
                        </div>
                        <div className="flex gap-2 justify-end">
                            <button
                                onClick={() => {
                                    setShowRejectModal(false);
                                    setRejectReason('');
                                }}
                                className="bg-gray-500 text-white px-4 py-2 rounded cursor-pointer hover:bg-gray-600"
                            >
                                Отмена
                            </button>
                            <button
                                onClick={rejectReturn}
                                className="bg-red-500 text-white px-4 py-2 rounded cursor-pointer hover:bg-red-600"
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
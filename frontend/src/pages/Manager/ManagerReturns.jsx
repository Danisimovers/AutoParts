import React, { useState } from 'react';
import api from '../../api/api';
import { useSearchAndFilters } from '../../hooks/useSearchAndFilters';
import { Search, Filter, ChevronDown, ChevronUp, X } from 'lucide-react';

function ManagerReturns() {
    const [showFilters, setShowFilters] = useState(false);
    const [selectedReturn, setSelectedReturn] = useState(null);
    const [rejectReason, setRejectReason] = useState('');
    const [showRejectModal, setShowRejectModal] = useState(false);

    // Используем хук для пагинации, поиска и фильтров
    const {
        data: returns,
        loading,
        totalItems,
        totalPages,
        currentPage,
        searchTerm,
        setSearchTerm,
        filters,
        updateFilter,
        clearFilters: clearAllFilters,
        getFilterCount,
        goToPage,
        reload: loadReturns
    } = useSearchAndFilters('/returns', {
        limit: 20,
        defaultFilters: {
            status: ''
        },
        enableSearch: true,
        enableFilters: true
    });

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
        switch(status) {
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'APPROVED': return 'bg-green-100 text-green-800';
            case 'REJECTED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
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

    if (loading && returns.length === 0) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Заявки на возврат</h1>
                <p className="text-sm text-gray-500 mt-1">Всего заявок: {totalItems}</p>
            </div>

            {/* ПОИСК И ФИЛЬТРЫ */}
            <div className="mb-4 flex flex-col gap-3">
                <div className="flex flex-col md:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                        <input
                            type="text"
                            placeholder="Поиск по ID заявки, ID заказа или ID пользователя..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                    </div>

                    <button
                        onClick={() => setShowFilters(!showFilters)}
                        className="flex items-center justify-center gap-2 px-4 py-2.5 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                    >
                        <Filter size={18} className="text-gray-500" />
                        <span className="text-sm text-gray-700">Фильтры</span>
                        {getFilterCount() > 0 && (
                            <span className="bg-orange-500 text-white text-xs px-2 py-0.5 rounded-full">
                                {getFilterCount()}
                            </span>
                        )}
                        {showFilters ? <ChevronUp size={18} className="text-gray-400" /> : <ChevronDown size={18} className="text-gray-400" />}
                    </button>
                </div>

                {/* Активные фильтры */}
                {getFilterCount() > 0 && (
                    <div className="flex flex-wrap gap-2">
                        <span className="text-xs text-gray-500">Активные фильтры:</span>
                        {filters.status && (
                            <span className="inline-flex items-center gap-1 px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded-full">
                                Статус: {getStatusText(filters.status)}
                                <X size={12} className="cursor-pointer hover:text-orange-900" onClick={() => updateFilter('status', '')} />
                            </span>
                        )}
                    </div>
                )}

                {/* Панель фильтров */}
                {showFilters && (
                    <div className="bg-gray-50 rounded-xl border border-gray-200 p-5 mt-2">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Статус заявки</label>
                                <select
                                    value={filters.status}
                                    onChange={(e) => updateFilter('status', e.target.value)}
                                    className="w-full p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                >
                                    <option value="">Все статусы</option>
                                    <option value="PENDING">Ожидает</option>
                                    <option value="APPROVED">Одобрен</option>
                                    <option value="REJECTED">Отклонен</option>
                                </select>
                            </div>
                        </div>
                        <div className="flex gap-3 mt-5 justify-end">
                            <button
                                onClick={clearAllFilters}
                                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition text-sm"
                            >
                                Сбросить все
                            </button>
                            <button
                                onClick={() => setShowFilters(false)}
                                className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition text-sm"
                            >
                                Применить
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {returns.length === 0 ? (
                <div className="text-center text-gray-400 py-10 bg-white rounded-xl border border-gray-200">
                    {searchTerm || getFilterCount() > 0
                        ? 'Ничего не найдено. Попробуйте изменить параметры поиска.'
                        : 'Нет заявок на возврат'}
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

            {/* Пагинация */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-6 flex-wrap">
                    <button
                        onClick={() => goToPage(1)}
                        disabled={currentPage === 1}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === 1
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        « Первая
                    </button>
                    <button
                        onClick={() => goToPage(currentPage - 1)}
                        disabled={currentPage === 1}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === 1
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        ← Назад
                    </button>

                    <div className="flex gap-1">
                        {[...Array(Math.min(totalPages, 10))].map((_, i) => {
                            let page;
                            if (totalPages <= 7) {
                                page = i + 1;
                            } else if (currentPage <= 4) {
                                page = i + 1;
                            } else if (currentPage >= totalPages - 3) {
                                page = totalPages - 9 + i;
                            } else {
                                page = currentPage - 4 + i;
                            }

                            if (page >= 1 && page <= totalPages) {
                                return (
                                    <button
                                        key={page}
                                        onClick={() => goToPage(page)}
                                        className={`w-8 h-8 rounded-md text-sm transition ${
                                            currentPage === page
                                                ? 'bg-orange-500 text-white cursor-default'
                                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                                        }`}
                                    >
                                        {page}
                                    </button>
                                );
                            }
                            return null;
                        })}
                    </div>

                    <button
                        onClick={() => goToPage(currentPage + 1)}
                        disabled={currentPage === totalPages}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === totalPages
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        Вперед →
                    </button>
                    <button
                        onClick={() => goToPage(totalPages)}
                        disabled={currentPage === totalPages}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === totalPages
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        Последняя »
                    </button>
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
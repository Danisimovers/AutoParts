import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSearchAndFilters } from '../../hooks/useSearchAndFilters';
import { Search, Filter, ChevronDown, ChevronUp, X } from 'lucide-react';

function VinRequests() {
    const navigate = useNavigate();
    const [showFilters, setShowFilters] = useState(false);

    // Используем хук для пагинации, поиска и фильтров
    const {
        data: requests,
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
        reload: loadRequests
    } = useSearchAndFilters('/manager/vin-requests', {
        limit: 20,
        defaultFilters: {
            status: ''
        },
        enableSearch: true,
        enableFilters: true
    });

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

    if (loading && requests.length === 0) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Заявки по VIN</h1>
                <p className="text-sm text-gray-500 mt-1">Всего заявок: {totalItems}</p>
            </div>

            {/* ПОИСК И ФИЛЬТРЫ */}
            <div className="mb-4 flex flex-col gap-3">
                <div className="flex flex-col md:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                        <input
                            type="text"
                            placeholder="Поиск по VIN или описанию..."
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
                                <label className="block text-sm font-medium text-gray-700 mb-2">Статус</label>
                                <select
                                    value={filters.status}
                                    onChange={(e) => updateFilter('status', e.target.value)}
                                    className="w-full p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                >
                                    <option value="">Все статусы</option>
                                    <option value="PENDING">Ожидает</option>
                                    <option value="PROCESSING">В обработке</option>
                                    <option value="COMPLETED">Выполнена</option>
                                    <option value="REJECTED">Отклонена</option>
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

            {requests.length === 0 && !loading && (
                <div className="text-center py-10 text-gray-400">
                    {searchTerm || getFilterCount() > 0
                        ? 'Ничего не найдено. Попробуйте изменить параметры поиска.'
                        : 'Нет заявок'}
                </div>
            )}

            {/* Пагинация */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-6 flex-wrap">
                    <button onClick={() => goToPage(1)} disabled={currentPage === 1} className="px-3 py-1.5 bg-gray-200 rounded-md text-sm disabled:opacity-50">«</button>
                    <button onClick={() => goToPage(currentPage - 1)} disabled={currentPage === 1} className="px-3 py-1.5 bg-gray-200 rounded-md text-sm disabled:opacity-50">←</button>
                    <span className="px-4 py-1.5 text-sm">{currentPage} / {totalPages}</span>
                    <button onClick={() => goToPage(currentPage + 1)} disabled={currentPage === totalPages} className="px-3 py-1.5 bg-gray-200 rounded-md text-sm disabled:opacity-50">→</button>
                    <button onClick={() => goToPage(totalPages)} disabled={currentPage === totalPages} className="px-3 py-1.5 bg-gray-200 rounded-md text-sm disabled:opacity-50">»</button>
                </div>
            )}
        </div>
    );
}

export default VinRequests;
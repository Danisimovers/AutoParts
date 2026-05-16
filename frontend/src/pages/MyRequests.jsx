// src/pages/MyRequests.js
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';
import {
    FileText,
    RotateCcw,
    CheckCircle,
    XCircle,
    Clock,
    MessageCircle,
    Search,
    Filter
} from 'lucide-react';

function MyRequests() {
    const { user } = useAuth();
    const [vinRequests, setVinRequests] = useState([]);
    const [returns, setReturns] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('vin'); // 'vin' or 'returns'
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        if (user) {
            loadRequests();
        }
    }, [user]);

    const loadRequests = async () => {
        setLoading(true);
        try {
            // Загружаем VIN-заявки пользователя
            const vinResponse = await api.get(`/vin-requests/user/${user.id}`);
            if (vinResponse.data.success) {
                setVinRequests(vinResponse.data.data || []);
            }

            // Загружаем заявки на возврат пользователя
            const returnsResponse = await api.get(`/returns/user/${user.id}`);
            if (returnsResponse.data.success) {
                setReturns(returnsResponse.data.data || []);
            }
        } catch (error) {
            console.error('Ошибка загрузки заявок:', error);
        } finally {
            setLoading(false);
        }
    };

    const getVinStatusClass = (status) => {
        switch(status) {
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'PROCESSING': return 'bg-blue-100 text-blue-800';
            case 'COMPLETED': return 'bg-green-100 text-green-800';
            case 'REJECTED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getVinStatusText = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'PROCESSING': return 'В обработке';
            case 'COMPLETED': return 'Выполнен';
            case 'REJECTED': return 'Отклонен';
            default: return status;
        }
    };

    const getReturnStatusClass = (status) => {
        switch(status) {
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'APPROVED': return 'bg-green-100 text-green-800';
            case 'REJECTED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getReturnStatusText = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'APPROVED': return 'Одобрен';
            case 'REJECTED': return 'Отклонен';
            default: return status;
        }
    };

    const filteredVinRequests = vinRequests.filter(request =>
        request.vin?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        request.id?.toString().includes(searchTerm)
    );

    const filteredReturns = returns.filter(ret =>
        ret.id?.toString().includes(searchTerm) ||
        ret.orderId?.toString().includes(searchTerm)
    );

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="text-gray-400">Загрузка...</div>
            </div>
        );
    }

    return (
        <div className="w-full px-4 py-8">
            <div className="max-w-6xl mx-auto">
                <h1 className="text-3xl font-bold text-gray-800 mb-8 flex items-center gap-3">
                    <FileText size={28} className="text-orange-500" />
                    Мои заявки
                </h1>

                {/* Поиск */}
                <div className="mb-6">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                        <input
                            type="text"
                            placeholder="Поиск по ID или VIN..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                    </div>
                </div>

                {/* Табы */}
                <div className="flex gap-2 mb-6 border-b border-gray-200">
                    <button
                        onClick={() => setActiveTab('vin')}
                        className={`px-6 py-3 font-medium transition rounded-t-lg ${
                            activeTab === 'vin'
                                ? 'bg-orange-500 text-white'
                                : 'text-gray-600 hover:bg-gray-100'
                        }`}
                    >
                        VIN-заявки ({vinRequests.length})
                    </button>
                    <button
                        onClick={() => setActiveTab('returns')}
                        className={`px-6 py-3 font-medium transition rounded-t-lg ${
                            activeTab === 'returns'
                                ? 'bg-orange-500 text-white'
                                : 'text-gray-600 hover:bg-gray-100'
                        }`}
                    >
                        Возвраты ({returns.length})
                    </button>
                </div>

                {/* VIN-заявки */}
                {activeTab === 'vin' && (
                    <>
                        {filteredVinRequests.length === 0 ? (
                            <div className="text-center py-12 bg-white rounded-xl border border-gray-200">
                                <FileText size={48} className="text-gray-300 mx-auto mb-4" />
                                <p className="text-gray-400">
                                    {searchTerm ? 'Ничего не найдено' : 'У вас пока нет VIN-заявок'}
                                </p>
                                {!searchTerm && (
                                    <button
                                        onClick={() => document.querySelector('button:contains("Запрос по VIN")')?.click()}
                                        className="mt-4 bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition"
                                    >
                                        Создать заявку
                                    </button>
                                )}
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {filteredVinRequests.map(request => (
                                    <div key={request.id} className="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-md transition">
                                        <div className="flex justify-between items-start mb-4">
                                            <div>
                                                <h3 className="text-lg font-bold text-gray-800">
                                                    Заявка #{request.id}
                                                </h3>
                                                <p className="text-sm text-gray-500 mt-1">
                                                    VIN: <span className="font-mono font-medium">{request.vin}</span>
                                                </p>
                                            </div>
                                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${getVinStatusClass(request.status)}`}>
                                                {getVinStatusText(request.status)}
                                            </span>
                                        </div>

                                        <div className="flex justify-between items-center">
                                            <div className="text-sm text-gray-500">
                                                Создана: {new Date(request.createdAt).toLocaleDateString()}
                                            </div>
                                            <Link
                                                to={`/vin-requests/${request.id}`}
                                                className="flex items-center gap-2 px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition"
                                            >
                                                <MessageCircle size={16} />
                                                Перейти в чат
                                            </Link>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}

                {/* Заявки на возврат */}
                {activeTab === 'returns' && (
                    <>
                        {filteredReturns.length === 0 ? (
                            <div className="text-center py-12 bg-white rounded-xl border border-gray-200">
                                <RotateCcw size={48} className="text-gray-300 mx-auto mb-4" />
                                <p className="text-gray-400">
                                    {searchTerm ? 'Ничего не найдено' : 'У вас пока нет заявок на возврат'}
                                </p>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {filteredReturns.map(ret => (
                                    <div key={ret.id} className="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-md transition">
                                        <div className="flex justify-between items-start mb-4">
                                            <div>
                                                <h3 className="text-lg font-bold text-gray-800">
                                                    Заявка #{ret.id}
                                                </h3>
                                                <p className="text-sm text-gray-500 mt-1">
                                                    Заказ №{ret.orderId}
                                                </p>
                                                <p className="text-sm text-gray-600 mt-2">
                                                    Причина: {ret.reason}
                                                </p>
                                            </div>
                                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${getReturnStatusClass(ret.status)}`}>
                                                {getReturnStatusText(ret.status)}
                                            </span>
                                        </div>

                                        <div className="text-sm text-gray-500">
                                            Создана: {new Date(ret.createdAt).toLocaleDateString()}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}

export default MyRequests;
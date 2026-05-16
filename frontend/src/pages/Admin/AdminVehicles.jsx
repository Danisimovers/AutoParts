// src/pages/Admin/AdminVehicles.js
import React, { useState } from 'react';
import api from '../../api/api';
import { useSearchAndFilters } from '../../hooks/useSearchAndFilters';
import { Search, X } from 'lucide-react';

function AdminVehicles() {
    // Используем хук с поиском и пагинацией
    const {
        data: vehicles,
        loading,
        totalItems,
        totalPages,
        currentPage,
        searchTerm,
        setSearchTerm,
        clearSearch,
        goToPage,
        reload: loadVehicles
    } = useSearchAndFilters('/admin/vehicles', {
        limit: 20,
        enableSearch: true,
        enableFilters: false
    });

    const [showForm, setShowForm] = useState(false);
    const [editingVehicle, setEditingVehicle] = useState(null);
    const [formData, setFormData] = useState({
        make: '',
        model: '',
        generation: '',
        yearFrom: '',
        yearTo: '',
        engine: ''
    });
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // Валидация
        if (!formData.make.trim()) {
            setError('Марка автомобиля обязательна');
            return;
        }
        if (!formData.model.trim()) {
            setError('Модель автомобиля обязательна');
            return;
        }

        try {
            if (editingVehicle) {
                await api.put(`/admin/vehicles/${editingVehicle.id}`, formData);
                alert('Автомобиль обновлен');
            } else {
                await api.post('/admin/vehicles', formData);
                alert('Автомобиль создан');
            }
            setShowForm(false);
            setEditingVehicle(null);
            setFormData({
                make: '',
                model: '',
                generation: '',
                yearFrom: '',
                yearTo: '',
                engine: ''
            });
            loadVehicles();
        } catch (error) {
            setError(error.response?.data?.message || 'Ошибка сохранения');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Удалить автомобиль? Это может повлиять на совместимость товаров.')) return;
        try {
            await api.delete(`/admin/vehicles/${id}`);
            alert('Автомобиль удален');
            if (vehicles.length === 1 && currentPage > 1) {
                goToPage(currentPage - 1);
            } else {
                loadVehicles();
            }
        } catch (error) {
            alert('Ошибка удаления');
        }
    };

    const handleEdit = (vehicle) => {
        setEditingVehicle(vehicle);
        setFormData({
            make: vehicle.make || '',
            model: vehicle.model || '',
            generation: vehicle.generation || '',
            yearFrom: vehicle.yearFrom || '',
            yearTo: vehicle.yearTo || '',
            engine: vehicle.engine || ''
        });
        setShowForm(true);
    };

    // Фильтрация на клиенте (так как бэкенд пока не поддерживает search)
    const filteredVehicles = vehicles.filter(vehicle =>
        vehicle.make?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        vehicle.model?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        vehicle.generation?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        vehicle.engine?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (loading && vehicles.length === 0) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Управление автомобилями</h1>
                    <p className="text-sm text-gray-500 mt-1">Всего моделей: {totalItems}</p>
                </div>
                <button
                    onClick={() => {
                        setEditingVehicle(null);
                        setFormData({
                            make: '',
                            model: '',
                            generation: '',
                            yearFrom: '',
                            yearTo: '',
                            engine: ''
                        });
                        setShowForm(true);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition"
                >
                    + Добавить автомобиль
                </button>
            </div>

            {/* Поиск */}
            <div className="mb-4">
                <div className="relative max-w-md">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                    <input
                        type="text"
                        placeholder="Поиск по марке, модели, поколению или двигателю..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-10 p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                    />
                    {searchTerm && (
                        <button
                            onClick={clearSearch}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        >
                            <X size={16} />
                        </button>
                    )}
                </div>
            </div>

            {/* Форма добавления/редактирования */}
            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-50 p-5 rounded-xl mb-6 border border-gray-200">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">
                        {editingVehicle ? 'Редактировать автомобиль' : 'Новый автомобиль'}
                    </h2>
                    {error && (
                        <div className="bg-red-50 text-red-700 p-2 rounded-lg mb-3 text-sm">
                            {error}
                        </div>
                    )}
                    <div className="grid grid-cols-2 gap-4">
                        <input
                            type="text"
                            placeholder="Марка *"
                            value={formData.make}
                            onChange={(e) => setFormData({ ...formData, make: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                            required
                        />
                        <input
                            type="text"
                            placeholder="Модель *"
                            value={formData.model}
                            onChange={(e) => setFormData({ ...formData, model: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                            required
                        />
                        <input
                            type="text"
                            placeholder="Поколение"
                            value={formData.generation}
                            onChange={(e) => setFormData({ ...formData, generation: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="number"
                            placeholder="Год выпуска (от)"
                            value={formData.yearFrom}
                            onChange={(e) => setFormData({ ...formData, yearFrom: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="number"
                            placeholder="Год выпуска (до)"
                            value={formData.yearTo}
                            onChange={(e) => setFormData({ ...formData, yearTo: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="text"
                            placeholder="Двигатель"
                            value={formData.engine}
                            onChange={(e) => setFormData({ ...formData, engine: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                    </div>
                    <div className="flex gap-3 mt-5 justify-end">
                        <button type="submit" className="bg-orange-500 text-white px-5 py-2 rounded-lg hover:bg-orange-600 transition">
                            Сохранить
                        </button>
                        <button type="button" onClick={() => setShowForm(false)} className="bg-gray-500 text-white px-5 py-2 rounded-lg hover:bg-gray-600 transition">
                            Отмена
                        </button>
                    </div>
                </form>
            )}

            {/* Таблица автомобилей */}
            <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                <table className="w-full">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Марка</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Модель</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Поколение</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Годы выпуска</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Двигатель</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredVehicles.map(vehicle => (
                        <tr key={vehicle.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{vehicle.id}</td>
                            <td className="p-3 text-sm font-medium text-gray-800">{vehicle.make}</td>
                            <td className="p-3 text-sm text-gray-600">{vehicle.model || '-'}</td>
                            <td className="p-3 text-sm text-gray-600">{vehicle.generation || '-'}</td>
                            <td className="text-center p-3 text-sm text-gray-600">
                                {vehicle.yearFrom || '-'} {vehicle.yearTo ? `- ${vehicle.yearTo}` : ''}
                            </td>
                            <td className="p-3 text-sm text-gray-600 max-w-xs truncate">
                                {vehicle.engine || '-'}
                            </td>
                            <td className="text-center p-3">
                                <button
                                    onClick={() => handleEdit(vehicle)}
                                    className="bg-blue-500 text-white px-3 py-1 rounded-md mr-2 hover:bg-blue-600 transition text-sm"
                                >
                                    Редакт
                                </button>
                                <button
                                    onClick={() => handleDelete(vehicle.id)}
                                    className="bg-red-500 text-white px-3 py-1 rounded-md hover:bg-red-600 transition text-sm"
                                >
                                    Удалить
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {filteredVehicles.length === 0 && !loading && (
                <div className="text-center py-10 text-gray-400">
                    {searchTerm
                        ? 'По вашему запросу ничего не найдено'
                        : 'Нет автомобилей. Нажмите "Добавить автомобиль" чтобы создать первый.'}
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
        </div>
    );
}

export default AdminVehicles;
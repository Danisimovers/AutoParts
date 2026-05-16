import React, { useState } from 'react';
import api from '../../api/api';
import { useSearchAndFilters } from '../../hooks/useSearchAndFilters';
import { Search, X } from 'lucide-react';

function AdminSuppliers() {
    const {
        data: suppliers,
        loading,
        totalItems,
        totalPages,
        currentPage,
        searchTerm,
        setSearchTerm,
        clearSearch,
        goToPage,
        reload: loadSuppliers
    } = useSearchAndFilters('/admin/suppliers', {
        limit: 20,
        enableSearch: true,
        enableFilters: false
    });

    const [showForm, setShowForm] = useState(false);
    const [editingSupplier, setEditingSupplier] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        contact: '',
        email: '',
        phone: '',
        address: '',
        apiUrl: '',
        apiKey: ''
    });
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            if (editingSupplier) {
                await api.put(`/admin/suppliers/${editingSupplier.id}`, formData);
                alert('Поставщик обновлен');
            } else {
                await api.post('/admin/suppliers', formData);
                alert('Поставщик добавлен');
            }
            setShowForm(false);
            setEditingSupplier(null);
            setFormData({ name: '', contact: '', email: '', phone: '', address: '', apiUrl: '', apiKey: '' });
            loadSuppliers();
        } catch (error) {
            setError(error.response?.data?.message || 'Ошибка сохранения');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Удалить поставщика? Внимание! Если есть связанные запросы, удаление может быть заблокировано.')) return;
        try {
            await api.delete(`/admin/suppliers/${id}`);
            alert('Поставщик удален');
            if (suppliers.length === 1 && currentPage > 1) {
                goToPage(currentPage - 1);
            } else {
                loadSuppliers();
            }
        } catch (error) {
            console.error('Ошибка удаления:', error);
            if (error.response?.status === 401) {
                alert('Сессия истекла. Пожалуйста, войдите заново.');
                // Перенаправление на логин произойдет автоматически
            } else if (error.response?.data?.message) {
                alert(error.response.data.message);
            } else {
                alert('Ошибка удаления. Возможно, у поставщика есть связанные запросы.');
            }
        }
    };

    const handleEdit = (supplier) => {
        setEditingSupplier(supplier);
        setFormData({
            name: supplier.name,
            contact: supplier.contact || '',
            email: supplier.email || '',
            phone: supplier.phone || '',
            address: supplier.address || '',
            apiUrl: supplier.apiUrl || '',
            apiKey: supplier.apiKey || ''
        });
        setShowForm(true);
    };

    if (loading && suppliers.length === 0) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Управление поставщиками</h1>
                    <p className="text-sm text-gray-500 mt-1">Всего поставщиков: {totalItems}</p>
                </div>
                <button
                    onClick={() => {
                        setEditingSupplier(null);
                        setFormData({ name: '', contact: '', email: '', phone: '', address: '', apiUrl: '', apiKey: '' });
                        setShowForm(true);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition"
                >
                    + Добавить поставщика
                </button>
            </div>

            {/* Поиск */}
            <div className="mb-4">
                <div className="relative max-w-md">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                    <input
                        type="text"
                        placeholder="Поиск по названию, контакту, email или телефону..."
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

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-50 p-5 rounded-xl mb-6 border border-gray-200">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">
                        {editingSupplier ? 'Редактировать поставщика' : 'Новый поставщик'}
                    </h2>
                    {error && (
                        <div className="bg-red-50 text-red-700 p-2 rounded-lg mb-3 text-sm">
                            {error}
                        </div>
                    )}
                    <div className="grid grid-cols-2 gap-4">
                        <input
                            type="text"
                            placeholder="Название *"
                            value={formData.name}
                            onChange={(e) => setFormData({...formData, name: e.target.value})}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                            required
                        />
                        <input
                            type="text"
                            placeholder="Контактное лицо"
                            value={formData.contact}
                            onChange={(e) => setFormData({...formData, contact: e.target.value})}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="email"
                            placeholder="Email"
                            value={formData.email}
                            onChange={(e) => setFormData({...formData, email: e.target.value})}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="tel"
                            placeholder="Телефон"
                            value={formData.phone}
                            onChange={(e) => setFormData({...formData, phone: e.target.value})}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="text"
                            placeholder="Адрес"
                            value={formData.address}
                            onChange={(e) => setFormData({...formData, address: e.target.value})}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 col-span-2"
                        />
                        <input
                            type="text"
                            placeholder="API URL"
                            value={formData.apiUrl}
                            onChange={(e) => setFormData({...formData, apiUrl: e.target.value})}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 col-span-2"
                        />
                        <input
                            type="text"
                            placeholder="API ключ"
                            value={formData.apiKey}
                            onChange={(e) => setFormData({...formData, apiKey: e.target.value})}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 col-span-2"
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

            <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                <table className="w-full">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Название</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Контакт</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Email</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Телефон</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {suppliers.map(supplier => (
                        <tr key={supplier.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{supplier.id}</td>
                            <td className="p-3 text-sm font-medium text-gray-800">{supplier.name}</td>
                            <td className="p-3 text-sm text-gray-600">{supplier.contact || '-'}</td>
                            <td className="p-3 text-sm text-gray-600">{supplier.email || '-'}</td>
                            <td className="p-3 text-sm text-gray-600">{supplier.phone || '-'}</td>
                            <td className="text-center p-3">
                                <button
                                    onClick={() => handleEdit(supplier)}
                                    className="bg-blue-500 text-white px-3 py-1 rounded-md mr-2 hover:bg-blue-600 transition text-sm"
                                >
                                    Редакт
                                </button>
                                <button
                                    onClick={() => handleDelete(supplier.id)}
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

            {suppliers.length === 0 && !loading && (
                <div className="text-center py-10 text-gray-400">
                    {searchTerm ? 'По вашему запросу ничего не найдено' : 'Нет поставщиков'}
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

export default AdminSuppliers;
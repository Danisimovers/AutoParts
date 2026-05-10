import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminManufacturers() {
    const [manufacturers, setManufacturers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingManufacturer, setEditingManufacturer] = useState(null);
    const [formData, setFormData] = useState({ name: '', country: '', contactInfo: '' });
    const [error, setError] = useState('');

    useEffect(() => {
        loadManufacturers();
    }, []);

    const loadManufacturers = async () => {
        setLoading(true);
        try {
            const response = await api.get('/admin/manufacturers');
            if (response.data.success) {
                setManufacturers(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки производителей:', error);
            alert('Ошибка загрузки производителей');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            if (editingManufacturer) {
                await api.put(`/admin/manufacturers/${editingManufacturer.id}`, formData);
                alert('Производитель обновлен');
            } else {
                await api.post('/admin/manufacturers', formData);
                alert('Производитель создан');
            }
            setShowForm(false);
            setEditingManufacturer(null);
            setFormData({ name: '', country: '', contactInfo: '' });
            loadManufacturers();
        } catch (error) {
            setError(error.response?.data?.message || 'Ошибка сохранения');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Удалить производителя? Товары этого производителя останутся без производителя.')) return;
        try {
            await api.delete(`/admin/manufacturers/${id}`);
            alert('Производитель удален');
            loadManufacturers();
        } catch (error) {
            alert('Ошибка удаления');
        }
    };

    const handleEdit = (manufacturer) => {
        setEditingManufacturer(manufacturer);
        setFormData({
            name: manufacturer.name,
            country: manufacturer.country || '',
            contactInfo: manufacturer.contactInfo || ''
        });
        setShowForm(true);
    };

    if (loading) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Управление производителями</h1>
                    <p className="text-sm text-gray-500 mt-1">Всего производителей: {manufacturers.length}</p>
                </div>
                <button
                    onClick={() => {
                        setEditingManufacturer(null);
                        setFormData({ name: '', country: '', contactInfo: '' });
                        setShowForm(true);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition"
                >
                    + Добавить производителя
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-50 p-5 rounded-xl mb-6 border border-gray-200">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">
                        {editingManufacturer ? 'Редактировать производителя' : 'Новый производитель'}
                    </h2>
                    {error && (
                        <div className="bg-red-50 text-red-700 p-2 rounded-lg mb-3 text-sm">
                            {error}
                        </div>
                    )}
                    <div className="grid grid-cols-2 gap-4">
                        <input
                            type="text"
                            placeholder="Название производителя *"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                            required
                        />
                        <input
                            type="text"
                            placeholder="Страна"
                            value={formData.country}
                            onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <textarea
                            placeholder="Контактная информация"
                            value={formData.contactInfo}
                            onChange={(e) => setFormData({ ...formData, contactInfo: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 col-span-2"
                            rows="3"
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
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Страна</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Контактная информация</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {manufacturers.map(man => (
                        <tr key={man.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{man.id}</td>
                            <td className="p-3 text-sm font-medium text-gray-800">{man.name}</td>
                            <td className="p-3 text-sm text-gray-600">{man.country || '-'}</td>
                            <td className="p-3 text-sm text-gray-600">{man.contactInfo || '-'}</td>
                            <td className="text-center p-3">
                                <button
                                    onClick={() => handleEdit(man)}
                                    className="bg-blue-500 text-white px-3 py-1 rounded-md mr-2 hover:bg-blue-600 transition text-sm"
                                >
                                    Редакт
                                </button>
                                <button
                                    onClick={() => handleDelete(man.id)}
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
        </div>
    );
}

export default AdminManufacturers;
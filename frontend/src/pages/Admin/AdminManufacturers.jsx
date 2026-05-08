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

    if (loading) return <div className="p-5">Загрузка...</div>;

    return (
        <div className="p-5">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Управление производителями</h1>
                <button
                    onClick={() => {
                        setEditingManufacturer(null);
                        setFormData({ name: '', country: '', contactInfo: '' });
                        setShowForm(true);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600"
                >
                    + Добавить производителя
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-100 p-4 rounded-lg mb-6">
                    <h2 className="text-xl font-bold mb-4">{editingManufacturer ? 'Редактировать' : 'Новый'} производитель</h2>
                    {error && <div className="bg-red-100 text-red-600 p-2 rounded mb-3">{error}</div>}
                    <div className="grid grid-cols-2 gap-4">
                        <input
                            type="text"
                            placeholder="Название производителя *"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            className="p-2 border rounded"
                            required
                        />
                        <input
                            type="text"
                            placeholder="Страна"
                            value={formData.country}
                            onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                            className="p-2 border rounded"
                        />
                        <textarea
                            placeholder="Контактная информация"
                            value={formData.contactInfo}
                            onChange={(e) => setFormData({ ...formData, contactInfo: e.target.value })}
                            className="p-2 border rounded col-span-2"
                            rows="3"
                        />
                    </div>
                    <div className="flex gap-2 mt-4">
                        <button type="submit" className="bg-green-500 text-white px-4 py-2 rounded">Сохранить</button>
                        <button type="button" onClick={() => setShowForm(false)} className="bg-gray-500 text-white px-4 py-2 rounded">Отмена</button>
                    </div>
                </form>
            )}

            <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                    <thead className="bg-gray-100">
                    <tr>
                        <th className="p-2 text-left">ID</th>
                        <th className="p-2 text-left">Название</th>
                        <th className="p-2 text-left">Страна</th>
                        <th className="p-2 text-left">Контактная информация</th>
                        <th className="p-2 text-center">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {manufacturers.map(man => (
                        <tr key={man.id} className="border-b">
                            <td className="p-2">{man.id}</td>
                            <td className="p-2">{man.name}</td>
                            <td className="p-2">{man.country || '-'}</td>
                            <td className="p-2">{man.contactInfo || '-'}</td>
                            <td className="p-2 text-center">
                                <button onClick={() => handleEdit(man)} className="bg-blue-500 text-white px-3 py-1 rounded mr-2 hover:bg-blue-600">✏️</button>
                                <button onClick={() => handleDelete(man.id)} className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">🗑️</button>
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
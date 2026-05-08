import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminCategories() {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingCategory, setEditingCategory] = useState(null);
    const [formData, setFormData] = useState({ name: '', description: '' });
    const [error, setError] = useState('');

    useEffect(() => {
        loadCategories();
    }, []);

    const loadCategories = async () => {
        setLoading(true);
        try {
            const response = await api.get('/admin/categories');
            if (response.data.success) {
                setCategories(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки категорий:', error);
            alert('Ошибка загрузки категорий');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            if (editingCategory) {
                await api.put(`/admin/categories/${editingCategory.id}`, formData);
                alert('Категория обновлена');
            } else {
                await api.post('/admin/categories', formData);
                alert('Категория создана');
            }
            setShowForm(false);
            setEditingCategory(null);
            setFormData({ name: '', description: '' });
            loadCategories();
        } catch (error) {
            setError(error.response?.data?.message || 'Ошибка сохранения');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Удалить категорию? Товары с этой категорией останутся без категории.')) return;
        try {
            await api.delete(`/admin/categories/${id}`);
            alert('Категория удалена');
            loadCategories();
        } catch (error) {
            alert('Ошибка удаления');
        }
    };

    const handleEdit = (category) => {
        setEditingCategory(category);
        setFormData({ name: category.name, description: category.description || '' });
        setShowForm(true);
    };

    if (loading) return <div className="p-5">Загрузка...</div>;

    return (
        <div className="p-5">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Управление категориями</h1>
                <button
                    onClick={() => {
                        setEditingCategory(null);
                        setFormData({ name: '', description: '' });
                        setShowForm(true);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600"
                >
                    + Добавить категорию
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-100 p-4 rounded-lg mb-6">
                    <h2 className="text-xl font-bold mb-4">{editingCategory ? 'Редактировать' : 'Новая'} категория</h2>
                    {error && <div className="bg-red-100 text-red-600 p-2 rounded mb-3">{error}</div>}
                    <div className="grid grid-cols-2 gap-4">
                        <input
                            type="text"
                            placeholder="Название категории"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            className="p-2 border rounded"
                            required
                        />
                        <textarea
                            placeholder="Описание"
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
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
                        <th className="p-2 text-left">Описание</th>
                        <th className="p-2 text-center">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {categories.map(cat => (
                        <tr key={cat.id} className="border-b">
                            <td className="p-2">{cat.id}</td>
                            <td className="p-2">{cat.name}</td>
                            <td className="p-2">{cat.description || '-'}</td>
                            <td className="p-2 text-center">
                                <button onClick={() => handleEdit(cat)} className="bg-blue-500 text-white px-3 py-1 rounded mr-2 hover:bg-blue-600">✏️</button>
                                <button onClick={() => handleDelete(cat.id)} className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">🗑️</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default AdminCategories;
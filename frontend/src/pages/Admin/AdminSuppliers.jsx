import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminSuppliers() {
    const [suppliers, setSuppliers] = useState([]);
    const [loading, setLoading] = useState(true);
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

    useEffect(() => {
        loadSuppliers();
    }, []);

    const loadSuppliers = async () => {
        setLoading(true);
        try {
            const response = await api.get('/admin/suppliers');
            if (response.data.success) {
                setSuppliers(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки поставщиков:', error);
            alert('Ошибка загрузки поставщиков');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
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
            alert('Ошибка сохранения');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Удалить поставщика?')) return;
        try {
            await api.delete(`/admin/suppliers/${id}`);
            alert('Поставщик удален');
            loadSuppliers();
        } catch (error) {
            alert('Ошибка удаления');
        }
    };

    if (loading) return <div className="p-5">Загрузка...</div>;

    return (
        <div className="p-5">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Управление поставщиками</h1>
                <button
                    onClick={() => {
                        setEditingSupplier(null);
                        setFormData({ name: '', contact: '', email: '', phone: '', address: '', apiUrl: '', apiKey: '' });
                        setShowForm(true);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600"
                >
                    + Добавить поставщика
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-100 p-4 rounded-lg mb-6">
                    <h2 className="text-xl font-bold mb-4">{editingSupplier ? 'Редактировать' : 'Новый'} поставщик</h2>
                    <div className="grid grid-cols-2 gap-4">
                        <input type="text" placeholder="Название" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} className="p-2 border rounded" required />
                        <input type="text" placeholder="Контактное лицо" value={formData.contact} onChange={(e) => setFormData({...formData, contact: e.target.value})} className="p-2 border rounded" />
                        <input type="email" placeholder="Email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} className="p-2 border rounded" />
                        <input type="tel" placeholder="Телефон" value={formData.phone} onChange={(e) => setFormData({...formData, phone: e.target.value})} className="p-2 border rounded" />
                        <input type="text" placeholder="Адрес" value={formData.address} onChange={(e) => setFormData({...formData, address: e.target.value})} className="p-2 border rounded col-span-2" />
                        <input type="text" placeholder="API URL" value={formData.apiUrl} onChange={(e) => setFormData({...formData, apiUrl: e.target.value})} className="p-2 border rounded col-span-2" />
                        <input type="text" placeholder="API ключ" value={formData.apiKey} onChange={(e) => setFormData({...formData, apiKey: e.target.value})} className="p-2 border rounded col-span-2" />
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
                        <th className="p-2 text-left">Контакт</th>
                        <th className="p-2 text-left">Email</th>
                        <th className="p-2 text-left">Телефон</th>
                        <th className="p-2 text-left">API URL</th>
                        <th className="p-2 text-center">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {suppliers.map(supplier => (
                        <tr key={supplier.id} className="border-b">
                            <td className="p-2">{supplier.id}</td>
                            <td className="p-2">{supplier.name}</td>
                            <td className="p-2">{supplier.contact || '-'}</td>
                            <td className="p-2">{supplier.email || '-'}</td>
                            <td className="p-2">{supplier.phone || '-'}</td>
                            <td className="p-2 text-sm truncate max-w-xs">{supplier.apiUrl || '-'}</td>
                            <td className="p-2 text-center">
                                <button onClick={() => {
                                    setEditingSupplier(supplier);
                                    setFormData(supplier);
                                    setShowForm(true);
                                }} className="bg-blue-500 text-white px-2 py-1 rounded mr-2">✏️</button>
                                <button onClick={() => handleDelete(supplier.id)} className="bg-red-500 text-white px-2 py-1 rounded">🗑️</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default AdminSuppliers;
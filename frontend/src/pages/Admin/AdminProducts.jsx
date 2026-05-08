import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminProducts() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [manufacturers, setManufacturers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);

    const [formData, setFormData] = useState({
        sku: '',
        name: '',
        description: '',
        price: '',
        categoryId: '',
        manufacturerId: '',
        oemCode: ''
    });

    useEffect(() => {
        loadProducts();
        loadCategories();
        loadManufacturers();
    }, []);

    const loadProducts = async () => {
        setLoading(true);
        try {
            const response = await api.get('/admin/products');
            if (response.data.success) {
                setProducts(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки товаров:', error);
            alert('Ошибка загрузки товаров');
        } finally {
            setLoading(false);
        }
    };

    const loadCategories = async () => {
        try {
            const response = await api.get('/admin/categories');
            if (response.data.success) {
                setCategories(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки категорий:', error);
        }
    };

    const loadManufacturers = async () => {
        try {
            const response = await api.get('/admin/manufacturers');
            if (response.data.success) {
                setManufacturers(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки производителей:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingProduct) {
                await api.put(`/admin/products/${editingProduct.id}`, formData);
                alert('Товар обновлен');
            } else {
                await api.post('/admin/products', formData);
                alert('Товар создан');
            }
            setShowForm(false);
            setEditingProduct(null);
            setFormData({ sku: '', name: '', description: '', price: '', categoryId: '', manufacturerId: '', oemCode: '' });
            loadProducts();
        } catch (error) {
            console.error('Ошибка сохранения:', error);
            alert('Ошибка сохранения товара');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Удалить товар?')) return;
        try {
            await api.delete(`/admin/products/${id}`);
            alert('Товар удален');
            loadProducts();
        } catch (error) {
            console.error('Ошибка удаления:', error);
            alert('Ошибка удаления');
        }
    };

    const handleEdit = (product) => {
        setEditingProduct(product);
        setFormData({
            sku: product.sku,
            name: product.name,
            description: product.description || '',
            price: product.price,
            categoryId: product.categoryId || '',
            manufacturerId: product.manufacturerId || '',
            oemCode: product.oemCode || ''
        });
        setShowForm(true);
    };

    if (loading) return <div className="p-5">Загрузка...</div>;

    return (
        <div className="p-5 flex-1">
            <div className="flex justify-between items-center mb-5">
                <h1 className="text-2xl font-bold">Управление товарами</h1>
                <button
                    onClick={() => {
                        setEditingProduct(null);
                        setFormData({ sku: '', name: '', description: '', price: '', categoryId: '', manufacturerId: '', oemCode: '' });
                        setShowForm(!showForm);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded cursor-pointer hover:bg-orange-600"
                >
                    {showForm ? 'Отмена' : '+ Добавить товар'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-100 p-5 rounded-lg mb-5">
                    <h3 className="text-lg font-bold mb-3">{editingProduct ? 'Редактировать товар' : 'Новый товар'}</h3>
                    <div className="grid grid-cols-2 gap-4">
                        <input type="text" placeholder="Артикул (SKU)" value={formData.sku} onChange={(e) => setFormData({ ...formData, sku: e.target.value })} required className="p-2 border rounded" />
                        <input type="text" placeholder="Название" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} required className="p-2 border rounded" />
                        <textarea placeholder="Описание" value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} rows="3" className="p-2 border rounded col-span-2" />
                        <input type="number" placeholder="Цена" value={formData.price} onChange={(e) => setFormData({ ...formData, price: e.target.value })} required className="p-2 border rounded" />
                        <input type="text" placeholder="OEM код" value={formData.oemCode} onChange={(e) => setFormData({ ...formData, oemCode: e.target.value })} className="p-2 border rounded" />

                        <select value={formData.categoryId} onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })} className="p-2 border rounded">
                            <option value="">Выберите категорию</option>
                            {categories.map(cat => (
                                <option key={cat.id} value={cat.id}>{cat.name}</option>
                            ))}
                        </select>

                        <select value={formData.manufacturerId} onChange={(e) => setFormData({ ...formData, manufacturerId: e.target.value })} className="p-2 border rounded">
                            <option value="">Выберите производителя</option>
                            {manufacturers.map(man => (
                                <option key={man.id} value={man.id}>{man.name}</option>
                            ))}
                        </select>
                    </div>
                    <div className="flex gap-2 mt-4 justify-end">
                        <button type="submit" className="bg-orange-500 text-white px-4 py-2 rounded cursor-pointer hover:bg-orange-600">
                            {editingProduct ? 'Обновить' : 'Создать'}
                        </button>
                        <button type="button" onClick={() => { setShowForm(false); setEditingProduct(null); }} className="bg-gray-500 text-white px-4 py-2 rounded cursor-pointer hover:bg-gray-600">
                            Отмена
                        </button>
                    </div>
                </form>
            )}

            <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                    <thead className="bg-gray-100">
                    <tr className="border-b">
                        <th className="text-left p-2">ID</th>
                        <th className="text-left p-2">Артикул</th>
                        <th className="text-left p-2">Название</th>
                        <th className="text-center p-2">Цена</th>
                        <th className="text-center p-2">Остаток</th>
                        <th className="text-center p-2">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {products.map(product => (
                        <tr key={product.id} className="border-b">
                            <td className="p-2">{product.id}</td>
                            <td className="p-2">{product.sku}</td>
                            <td className="p-2">{product.name}</td>
                            <td className="text-center p-2">{product.price} ₽</td>
                            <td className="text-center p-2">{product.stock}</td>
                            <td className="text-center p-2">
                                <button onClick={() => handleEdit(product)} className="bg-blue-500 text-white px-3 py-1 rounded mr-2 cursor-pointer hover:bg-blue-600">✏️</button>
                                <button onClick={() => handleDelete(product.id)} className="bg-red-500 text-white px-3 py-1 rounded cursor-pointer hover:bg-red-600">🗑️</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default AdminProducts;
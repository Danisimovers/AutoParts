import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminProducts() {
    const [products, setProducts] = useState([]);
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

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    return (
        <div style={{ padding: '20px', flex: 1 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h1>Управление товарами</h1>
                <button
                    onClick={() => {
                        setEditingProduct(null);
                        setFormData({ sku: '', name: '', description: '', price: '', categoryId: '', manufacturerId: '', oemCode: '' });
                        setShowForm(!showForm);
                    }}
                    style={{ padding: '10px 20px', backgroundColor: '#e67e22', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                    {showForm ? 'Отмена' : 'Добавить товар'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} style={{ backgroundColor: '#f5f5f5', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
                    <h3>{editingProduct ? 'Редактировать товар' : 'Новый товар'}</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                        <input type="text" placeholder="Артикул (SKU)" value={formData.sku} onChange={(e) => setFormData({ ...formData, sku: e.target.value })} required style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} />
                        <input type="text" placeholder="Название" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} required style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} />
                        <textarea placeholder="Описание" value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} rows="3" style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px', gridColumn: 'span 2' }} />
                        <input type="number" placeholder="Цена" value={formData.price} onChange={(e) => setFormData({ ...formData, price: e.target.value })} required style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} />
                        <input type="text" placeholder="OEM код" value={formData.oemCode} onChange={(e) => setFormData({ ...formData, oemCode: e.target.value })} style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} />
                        <input type="text" placeholder="ID категории" value={formData.categoryId} onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })} style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} />
                        <input type="text" placeholder="ID производителя" value={formData.manufacturerId} onChange={(e) => setFormData({ ...formData, manufacturerId: e.target.value })} style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} />
                    </div>
                    <div style={{ marginTop: '15px', display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                        <button type="submit" style={{ padding: '10px 20px', backgroundColor: '#e67e22', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                            {editingProduct ? 'Обновить' : 'Создать'}
                        </button>
                        <button type="button" onClick={() => { setShowForm(false); setEditingProduct(null); }} style={{ padding: '10px 20px', backgroundColor: '#666', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                            Отмена
                        </button>
                    </div>
                </form>
            )}

            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>ID</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Артикул</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Название</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Цена</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Остаток</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Действия</th>
                </tr>
                </thead>
                <tbody>
                {products.map(product => (
                    <tr key={product.id} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '10px' }}>{product.id}</td>
                        <td style={{ padding: '10px' }}>{product.sku}</td>
                        <td style={{ padding: '10px' }}>{product.name}</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>{product.price} ₽</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>{product.stock}</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <button onClick={() => handleEdit(product)} style={{ marginRight: '10px', padding: '5px 10px', cursor: 'pointer' }}>Редакт.</button>
                            <button onClick={() => handleDelete(product.id)} style={{ padding: '5px 10px', backgroundColor: '#ff4444', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Удалить</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default AdminProducts;
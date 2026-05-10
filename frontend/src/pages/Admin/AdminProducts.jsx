import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminProducts() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [manufacturers, setManufacturers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);

    // Пагинация
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const itemsPerPage = 10;

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
    }, [currentPage]);

    const loadProducts = async () => {
        setLoading(true);
        try {
            const response = await api.get('/admin/products', {
                params: {
                    page: currentPage,
                    limit: itemsPerPage
                }
            });
            if (response.data.success) {
                setProducts(response.data.data);
                setTotalPages(response.data.totalPages || Math.ceil(response.data.total / itemsPerPage));
                setTotalItems(response.data.total || response.data.data.length);
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
            if (products.length === 1 && currentPage > 1) {
                setCurrentPage(currentPage - 1);
            } else {
                loadProducts();
            }
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

    const goToPage = (page) => {
        if (page >= 1 && page <= totalPages) {
            setCurrentPage(page);
        }
    };

    if (loading && products.length === 0) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка товаров...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Управление товарами</h1>
                    <p className="text-sm text-gray-500 mt-1">Всего товаров: {totalItems}</p>
                </div>
                <button
                    onClick={() => {
                        setEditingProduct(null);
                        setFormData({ sku: '', name: '', description: '', price: '', categoryId: '', manufacturerId: '', oemCode: '' });
                        setShowForm(!showForm);
                    }}
                    className="bg-orange-500 text-white px-4 py-2 rounded-lg cursor-pointer hover:bg-orange-600 transition"
                >
                    {showForm ? 'Отмена' : '+ Добавить товар'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-gray-50 p-6 rounded-xl mb-6 border border-gray-200">
                    <h3 className="text-lg font-bold text-gray-800 mb-4">
                        {editingProduct ? 'Редактировать товар' : 'Новый товар'}
                    </h3>
                    <div className="grid grid-cols-2 gap-4">
                        <input
                            type="text"
                            placeholder="Артикул (SKU)"
                            value={formData.sku}
                            onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                            required
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="text"
                            placeholder="Название"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            required
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <textarea
                            placeholder="Описание"
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            rows="3"
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 col-span-2"
                        />
                        <input
                            type="number"
                            placeholder="Цена"
                            value={formData.price}
                            onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                            required
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                        <input
                            type="text"
                            placeholder="OEM код"
                            value={formData.oemCode}
                            onChange={(e) => setFormData({ ...formData, oemCode: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />

                        <select
                            value={formData.categoryId}
                            onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                        >
                            <option value="">Выберите категорию</option>
                            {categories.map(cat => (
                                <option key={cat.id} value={cat.id}>{cat.name}</option>
                            ))}
                        </select>

                        <select
                            value={formData.manufacturerId}
                            onChange={(e) => setFormData({ ...formData, manufacturerId: e.target.value })}
                            className="p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                        >
                            <option value="">Выберите производителя</option>
                            {manufacturers.map(man => (
                                <option key={man.id} value={man.id}>{man.name}</option>
                            ))}
                        </select>
                    </div>
                    <div className="flex gap-3 mt-5 justify-end">
                        <button
                            type="submit"
                            className="bg-orange-500 text-white px-5 py-2 rounded-lg cursor-pointer hover:bg-orange-600 transition"
                        >
                            {editingProduct ? 'Обновить' : 'Создать'}
                        </button>
                        <button
                            type="button"
                            onClick={() => { setShowForm(false); setEditingProduct(null); }}
                            className="bg-gray-500 text-white px-5 py-2 rounded-lg cursor-pointer hover:bg-gray-600 transition"
                        >
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
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Артикул</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Название</th>
                        <th className="text-right p-3 text-sm font-semibold text-gray-600">Цена</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Остаток</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {products.map(product => (
                        <tr key={product.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{product.id}</td>
                            <td className="p-3 text-sm font-mono text-gray-600">{product.sku}</td>
                            <td className="p-3 text-sm font-medium text-gray-800">{product.name}</td>
                            <td className="text-right p-3 text-sm font-semibold text-gray-800">
                                {product.price.toLocaleString()} ₽
                            </td>
                            <td className="text-center p-3 text-sm text-gray-600">
                                {product.stock}
                            </td>
                            <td className="text-center p-3">
                                <button
                                    onClick={() => handleEdit(product)}
                                    className="bg-blue-500 text-white px-3 py-1 rounded-md mr-2 cursor-pointer hover:bg-blue-600 transition text-sm"
                                >
                                    Редакт
                                </button>
                                <button
                                    onClick={() => handleDelete(product.id)}
                                    className="bg-red-500 text-white px-3 py-1 rounded-md cursor-pointer hover:bg-red-600 transition text-sm"
                                >
                                    Удалить
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {products.length === 0 && !loading && (
                <div className="text-center py-10 text-gray-400">
                    Нет товаров. Нажмите "Добавить товар" чтобы создать первый.
                </div>
            )}

            {/* Пагинация */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-6">
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
                        {[...Array(totalPages)].map((_, i) => {
                            const page = i + 1;
                            if (
                                page === 1 ||
                                page === totalPages ||
                                (page >= currentPage - 1 && page <= currentPage + 1)
                            ) {
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
                            } else if (
                                (page === currentPage - 2 && currentPage > 3) ||
                                (page === currentPage + 2 && currentPage < totalPages - 2)
                            ) {
                                return <span key={page} className="px-1 text-gray-400">...</span>;
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

export default AdminProducts;
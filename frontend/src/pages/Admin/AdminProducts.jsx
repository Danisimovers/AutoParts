import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useSearchAndFilters } from '../../hooks/useSearchAndFilters';
import { Search, Filter, ChevronDown, ChevronUp, X } from 'lucide-react';

function AdminProducts() {
    // Используем хук
    const {
        data: products,
        loading,
        totalItems,
        totalPages,
        currentPage,
        searchTerm,
        setSearchTerm,
        filters,
        updateFilter,
        clearFilters: clearAllFilters,
        getFilterCount,
        goToPage
    } = useSearchAndFilters('/admin/products', {
        limit: 20,
        defaultFilters: {
            categoryId: '',
            manufacturerId: '',
            vehicleId: ''
        }
    });

    // Локальные состояния
    const [categories, setCategories] = useState([]);
    const [manufacturers, setManufacturers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [showFilters, setShowFilters] = useState(false);

    const [formData, setFormData] = useState({
        sku: '',
        name: '',
        description: '',
        price: '',
        categoryId: '',
        manufacturerId: '',
        oemCode: ''
    });

    // Загрузка справочников
    useEffect(() => {
        loadCategories();
        loadManufacturers();
        loadVehicles();
    }, []);

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

    const loadVehicles = async () => {
        try {
            const response = await api.get('/vehicles');
            if (response.data.success) {
                setVehicles(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки автомобилей:', error);
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
                goToPage(currentPage - 1);
            } else {
                // Перезагружаем через хук
                window.location.reload(); // или можно добавить reload в хук
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

    const applyFilters = () => {
        setShowFilters(false);
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

            {/* ПОИСК И ФИЛЬТРЫ */}
            <div className="mb-4 flex flex-col gap-3">
                <div className="flex flex-col md:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                        <input
                            type="text"
                            placeholder="Поиск по артикулу (SKU), названию или OEM коду..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                        />
                    </div>

                    <button
                        onClick={() => setShowFilters(!showFilters)}
                        className="flex items-center justify-center gap-2 px-4 py-2.5 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                    >
                        <Filter size={18} className="text-gray-500" />
                        <span className="text-sm text-gray-700">Фильтры</span>
                        {getFilterCount() > 0 && (
                            <span className="bg-orange-500 text-white text-xs px-2 py-0.5 rounded-full">
                                {getFilterCount()}
                            </span>
                        )}
                        {showFilters ? <ChevronUp size={18} className="text-gray-400" /> : <ChevronDown size={18} className="text-gray-400" />}
                    </button>
                </div>

                {/* Активные фильтры */}
                {getFilterCount() > 0 && (
                    <div className="flex flex-wrap gap-2">
                        <span className="text-xs text-gray-500">Активные фильтры:</span>
                        {filters.categoryId && (
                            <span className="inline-flex items-center gap-1 px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded-full">
                                {categories.find(c => c.id == filters.categoryId)?.name}
                                <X size={12} className="cursor-pointer hover:text-orange-900" onClick={() => updateFilter('categoryId', '')} />
                            </span>
                        )}
                        {filters.manufacturerId && (
                            <span className="inline-flex items-center gap-1 px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded-full">
                                {manufacturers.find(m => m.id == filters.manufacturerId)?.name}
                                <X size={12} className="cursor-pointer hover:text-orange-900" onClick={() => updateFilter('manufacturerId', '')} />
                            </span>
                        )}
                        {filters.vehicleId && (
                            <span className="inline-flex items-center gap-1 px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded-full">
                                {vehicles.find(v => v.id == filters.vehicleId)?.make} {vehicles.find(v => v.id == filters.vehicleId)?.model}
                                <X size={12} className="cursor-pointer hover:text-orange-900" onClick={() => updateFilter('vehicleId', '')} />
                            </span>
                        )}
                    </div>
                )}

                {/* Панель фильтров */}
                {showFilters && (
                    <div className="bg-gray-50 rounded-xl border border-gray-200 p-5 mt-2">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Категория</label>
                                <select
                                    value={filters.categoryId}
                                    onChange={(e) => updateFilter('categoryId', e.target.value)}
                                    className="w-full p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                >
                                    <option value="">Все категории</option>
                                    {categories.map(cat => (
                                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Производитель</label>
                                <select
                                    value={filters.manufacturerId}
                                    onChange={(e) => updateFilter('manufacturerId', e.target.value)}
                                    className="w-full p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                >
                                    <option value="">Все производители</option>
                                    {manufacturers.map(man => (
                                        <option key={man.id} value={man.id}>{man.name}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Автомобиль</label>
                                <select
                                    value={filters.vehicleId}
                                    onChange={(e) => updateFilter('vehicleId', e.target.value)}
                                    className="w-full p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                >
                                    <option value="">Все автомобили</option>
                                    {vehicles.map(vehicle => (
                                        <option key={vehicle.id} value={vehicle.id}>
                                            {vehicle.make} {vehicle.model}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>
                        <div className="flex gap-3 mt-5 justify-end">
                            <button
                                onClick={clearAllFilters}
                                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition text-sm"
                            >
                                Сбросить все
                            </button>
                            <button
                                onClick={applyFilters}
                                className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition text-sm"
                            >
                                Применить
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Форма добавления/редактирования */}
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

            {/* Таблица товаров */}
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
                                {product.price?.toLocaleString()} ₽
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
                    {searchTerm || getFilterCount() > 0
                        ? 'Ничего не найдено. Попробуйте изменить параметры поиска.'
                        : 'Нет товаров. Нажмите "Добавить товар" чтобы создать первый.'}
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

export default AdminProducts;
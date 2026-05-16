import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useSearchAndFilters } from '../../hooks/useSearchAndFilters';
import { Search, X, Filter, ChevronDown, ChevronUp } from 'lucide-react';

function AdminStock() {
    const {
        data: productsData,
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
        goToPage,
        reload: loadProducts
    } = useSearchAndFilters('/admin/products', {
        limit: 20,
        defaultFilters: {
            categoryId: '',
            manufacturerId: '',
            vehicleId: ''
        }
    });

    const [categories, setCategories] = useState([]);
    const [manufacturers, setManufacturers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [showFilters, setShowFilters] = useState(false);
    const [updating, setUpdating] = useState(false);
    const [editingStockId, setEditingStockId] = useState(null);
    const [editValue, setEditValue] = useState({});

    useEffect(() => {
        loadCategories();
        loadManufacturers();
        loadVehicles();
    }, []);

    // ИСПРАВЛЕНО: правильное получение данных из пагинированного ответа
    const loadCategories = async () => {
        try {
            const response = await api.get('/admin/categories', {
                params: { page: 1, limit: 100 }
            });
            if (response.data.success) {
                const categoriesData = response.data.data.data || [];
                setCategories(categoriesData);
            }
        } catch (error) {
            console.error('Ошибка загрузки категорий:', error);
        }
    };

    const loadManufacturers = async () => {
        try {
            const response = await api.get('/admin/manufacturers', {
                params: { page: 1, limit: 100 }
            });
            if (response.data.success) {
                const manufacturersData = response.data.data.data || [];
                setManufacturers(manufacturersData);
            }
        } catch (error) {
            console.error('Ошибка загрузки производителей:', error);
        }
    };

    const loadVehicles = async () => {
        try {
            const response = await api.get('/vehicles');
            if (response.data.success) {
                const vehiclesData = response.data.data || [];
                setVehicles(Array.isArray(vehiclesData) ? vehiclesData : []);
            }
        } catch (error) {
            console.error('Ошибка загрузки автомобилей:', error);
        }
    };

    const startEdit = (product) => {
        setEditingStockId(product.id);
        setEditValue({ [product.id]: product.stock });
    };

    const cancelEdit = () => {
        setEditingStockId(null);
        setEditValue({});
    };

    const handleEditChange = (productId, value) => {
        setEditValue(prev => ({
            ...prev,
            [productId]: parseInt(value) || 0
        }));
    };

    const updateStock = async (productId) => {
        const quantity = editValue[productId];
        if (quantity === undefined || quantity === null) return;

        setUpdating(true);
        try {
            await api.put(`/admin/stock/${productId}?quantity=${quantity}`);
            alert('Остатки обновлены');
            setEditingStockId(null);
            setEditValue({});
            loadProducts();
        } catch (error) {
            console.error('Ошибка обновления остатков:', error);
            alert('Ошибка обновления остатков');
        } finally {
            setUpdating(false);
        }
    };

    const applyFilters = () => {
        setShowFilters(false);
    };

    if (loading && productsData.length === 0) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Управление остатками</h1>
                    <p className="text-sm text-gray-500 mt-1">Всего товаров: {totalItems}</p>
                </div>
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

            <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                <table className="w-full">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Артикул</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Название</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Текущий остаток</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Новое значение</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600"></th>
                    </tr>
                    </thead>
                    <tbody>
                    {productsData.map(product => (
                        <tr key={product.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{product.id}</td>
                            <td className="p-3 text-sm font-mono text-gray-600">{product.sku}</td>
                            <td className="p-3 text-sm font-medium text-gray-800">{product.name}</td>
                            <td className="text-center p-3 text-sm font-medium text-gray-800">
                                {product.stock} шт.
                            </td>
                            <td className="text-center p-3">
                                {editingStockId === product.id ? (
                                    <input
                                        type="number"
                                        value={editValue[product.id] ?? product.stock}
                                        onChange={(e) => handleEditChange(product.id, e.target.value)}
                                        className="w-28 px-3 py-1.5 text-center border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                                        autoFocus
                                    />
                                ) : (
                                    <span className="text-sm text-gray-400">—</span>
                                )}
                            </td>
                            <td className="text-center p-3">
                                {editingStockId === product.id ? (
                                    <div className="flex gap-2 justify-center">
                                        <button
                                            onClick={() => updateStock(product.id)}
                                            disabled={updating}
                                            className="bg-green-500 text-white px-3 py-1.5 rounded-lg hover:bg-green-600 transition text-sm disabled:opacity-50"
                                        >
                                            Сохранить
                                        </button>
                                        <button
                                            onClick={cancelEdit}
                                            className="bg-gray-500 text-white px-3 py-1.5 rounded-lg hover:bg-gray-600 transition text-sm"
                                        >
                                            Отмена
                                        </button>
                                    </div>
                                ) : (
                                    <button
                                        onClick={() => startEdit(product)}
                                        className="bg-orange-500 text-white px-4 py-1.5 rounded-lg hover:bg-orange-600 transition text-sm"
                                    >
                                        Изменить
                                    </button>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {productsData.length === 0 && !loading && (
                <div className="text-center py-10 text-gray-400">
                    {searchTerm || getFilterCount() > 0
                        ? 'Ничего не найдено. Попробуйте изменить параметры поиска.'
                        : 'Нет товаров'}
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

export default AdminStock;
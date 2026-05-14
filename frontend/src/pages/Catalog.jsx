import React, { useState, useEffect } from 'react';
import api from '../api/api';
import ProductCard from '../components/ProductCard';
import { Search, Filter, ChevronDown, ChevronUp, X } from 'lucide-react';

function Catalog() {
    const [externalProducts, setExternalProducts] = useState([]);
    const [loadingExternal, setLoadingExternal] = useState(false);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchType, setSearchType] = useState('startsWith');
    const [vehicles, setVehicles] = useState([]);
    const [selectedVehicle, setSelectedVehicle] = useState('');
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('');
    const [manufacturers, setManufacturers] = useState([]);
    const [selectedManufacturer, setSelectedManufacturer] = useState('');
    const [showFilters, setShowFilters] = useState(false);

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalItems, setTotalItems] = useState(0);
    const pageSize = 12;

    // Загрузка при монтировании и при смене фильтров
    useEffect(() => {
        // Если есть активный поисковый запрос - не загружаем обычные товары
        if (!searchQuery) {
            loadProducts();
            // Загружаем случайные товары от поставщиков для отображения (первые 3-5)
            loadExternalFallback();
        }
        loadVehicles();
        loadCategories();
        loadManufacturers();
    }, [currentPage, selectedCategory, selectedManufacturer, selectedVehicle]);

    // Загрузка товаров с бэка
    const loadProducts = async () => {
        setLoading(true);
        try {
            let url = `/products/page?page=${currentPage}&size=${pageSize}`;
            if (selectedCategory) url += `&categoryId=${selectedCategory}`;
            if (selectedManufacturer) url += `&manufacturerId=${selectedManufacturer}`;
            if (selectedVehicle) url += `&vehicleId=${selectedVehicle}`;

            const response = await api.get(url);
            if (response.data.success) {
                const data = response.data.data;
                setProducts(data.products);
                setTotalPages(data.totalPages);
                setTotalItems(data.totalItems);
            }
        } catch (error) {
            console.error('Ошибка загрузки товаров:', error);
        } finally {
            setLoading(false);
        }
    };

    // Загрузка случайных товаров от поставщиков для отображения (когда нет поиска)
    const loadExternalFallback = async () => {
        setLoadingExternal(true);
        try {
            // Загружаем популярные/последние товары от поставщиков
            const response = await api.get('/search/external/fallback');
            if (response.data.success) {
                setExternalProducts(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки товаров поставщиков:', error);
        } finally {
            setLoadingExternal(false);
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

    const loadCategories = async () => {
        try {
            const response = await api.get('/categories');
            if (response.data.success) {
                setCategories(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки категорий:', error);
        }
    };

    const loadManufacturers = async () => {
        try {
            const response = await api.get('/manufacturers');
            if (response.data.success) {
                setManufacturers(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки производителей:', error);
        }
    };

    // Поиск у поставщиков (общая функция)
    const searchExternal = async (query) => {
        if (!query || query.trim() === '') {
            setExternalProducts([]);
            return;
        }
        setLoadingExternal(true);
        try {
            // Передаём тип поиска на бэк
            const response = await api.get(`/search/external?query=${encodeURIComponent(query)}&searchType=${searchType}`);
            if (response.data.success) {
                setExternalProducts(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка поиска у поставщиков:', error);
        } finally {
            setLoadingExternal(false);
        }
    };

    // Основной поиск (товары + поставщики)
    const handleSearch = async () => {
        setLoading(true);
        try {
            // 1. Поиск по товарам на складе
            let url = '/search';
            const params = [];

            if (searchQuery) {
                params.push(`query=${encodeURIComponent(searchQuery)}`);
                params.push(`searchType=${searchType}`);
            }

            if (selectedVehicle) params.push(`vehicleId=${selectedVehicle}`);
            if (selectedCategory) params.push(`categoryId=${selectedCategory}`);
            if (selectedManufacturer) params.push(`manufacturerId=${selectedManufacturer}`);

            if (params.length > 0) url += '?' + params.join('&');

            const response = await api.get(url);
            if (response.data.success) {
                setProducts(response.data.data.products || []);
                setTotalPages(1);
            }
        } catch (error) {
            console.error('Ошибка поиска:', error);
        } finally {
            setLoading(false);
        }

        // 2. Поиск у поставщиков (если есть запрос)
        if (searchQuery && searchQuery.trim() !== '') {
            await searchExternal(searchQuery);
        } else {
            // Если нет запроса, показываем fallback товары
            loadExternalFallback();
        }
    };

    const addToCart = async (product) => {
        try {
            const response = await api.post('/cart/add', {
                productId: product.id,
                quantity: 1
            });
            if (response.data.success) {
                alert(`${product.name} добавлен в корзину`);
            }
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Ошибка добавления в корзину');
        }
    };

    const addExternalToCart = async (product) => {
        try {
            const response = await api.post('/cart/add-external', {
                productName: product.name,
                factoryNumber: product.factoryNumber,
                producer: product.producer,
                supplierName: product.supplierName,
                price: product.price,
                quantity: 1
            });
            if (response.data.success) {
                alert(`${product.name} добавлен в корзину (товар поставщика)`);
            }
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Ошибка добавления в корзину');
        }
    };

    const clearFilters = () => {
        setSelectedCategory('');
        setSelectedManufacturer('');
        setSelectedVehicle('');
    };

    const applyFilters = () => {
        setCurrentPage(0);
        loadProducts();
        setShowFilters(false);
    };

    const goToPage = (page) => {
        if (page >= 0 && page < totalPages) {
            setCurrentPage(page);
        }
    };

    const getPlaceholder = () => {
        switch(searchType) {
            case 'startsWith': return 'Артикул начинается с... например: 0357';
            case 'exact': return 'Точный артикул... например: D-OIL-001';
            case 'name': return 'Название товара...';
            default: return 'Поиск...';
        }
    };

    const getFilterCount = () => {
        let count = 0;
        if (selectedCategory) count++;
        if (selectedManufacturer) count++;
        if (selectedVehicle) count++;
        return count;
    };

    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-8 text-center">Каталог автозапчастей</h1>

            <div className="flex flex-col lg:flex-row gap-6">
                {/* Левая панель */}
                <div className="w-full lg:w-80 flex-shrink-0">
                    <button
                        onClick={() => setShowFilters(!showFilters)}
                        className="w-full flex items-center justify-between gap-2 px-4 py-3 bg-white rounded-xl border border-gray-200 shadow-sm hover:border-orange-300 hover:shadow-md transition"
                    >
                        <div className="flex items-center gap-2">
                            <Filter size={18} className="text-gray-500" />
                            <span className="text-sm font-medium text-gray-700">Фильтры</span>
                            {getFilterCount() > 0 && (
                                <span className="bg-orange-500 text-white text-xs px-2 py-0.5 rounded-full">
                                    {getFilterCount()}
                                </span>
                            )}
                        </div>
                        {showFilters ? <ChevronUp size={18} className="text-gray-400" /> : <ChevronDown size={18} className="text-gray-400" />}
                    </button>

                    {getFilterCount() > 0 && (
                        <div className="mt-3 flex flex-wrap gap-2">
                            <span className="text-xs text-gray-500">Активные фильтры:</span>
                            {selectedCategory && (
                                <span className="inline-flex items-center gap-1 px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded-full">
                                    {categories.find(c => c.id == selectedCategory)?.name}
                                    <X size={12} className="cursor-pointer hover:text-orange-900" onClick={() => setSelectedCategory('')} />
                                </span>
                            )}
                            {selectedManufacturer && (
                                <span className="inline-flex items-center gap-1 px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded-full">
                                    {manufacturers.find(m => m.id == selectedManufacturer)?.name}
                                    <X size={12} className="cursor-pointer hover:text-orange-900" onClick={() => setSelectedManufacturer('')} />
                                </span>
                            )}
                            {selectedVehicle && (
                                <span className="inline-flex items-center gap-1 px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded-full">
                                    {vehicles.find(v => v.id == selectedVehicle)?.make} {vehicles.find(v => v.id == selectedVehicle)?.model}
                                    <X size={12} className="cursor-pointer hover:text-orange-900" onClick={() => setSelectedVehicle('')} />
                                </span>
                            )}
                        </div>
                    )}

                    {showFilters && (
                        <div className="mt-3 bg-white rounded-xl border border-gray-100 shadow-lg p-5">
                            <div className="space-y-5">
                                <div>
                                    <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Категория</label>
                                    <select
                                        value={selectedCategory}
                                        onChange={(e) => setSelectedCategory(e.target.value)}
                                        className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 bg-gray-50"
                                    >
                                        <option value="">Все категории</option>
                                        {categories.map(cat => (
                                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Производитель</label>
                                    <select
                                        value={selectedManufacturer}
                                        onChange={(e) => setSelectedManufacturer(e.target.value)}
                                        className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 bg-gray-50"
                                    >
                                        <option value="">Все производители</option>
                                        {manufacturers.map(man => (
                                            <option key={man.id} value={man.id}>{man.name}</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Автомобиль</label>
                                    <select
                                        value={selectedVehicle}
                                        onChange={(e) => setSelectedVehicle(e.target.value)}
                                        className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 bg-gray-50"
                                    >
                                        <option value="">Все автомобили</option>
                                        {vehicles.map(vehicle => (
                                            <option key={vehicle.id} value={vehicle.id}>
                                                {vehicle.make} {vehicle.model}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div className="flex gap-3 pt-2">
                                    <button
                                        onClick={clearFilters}
                                        className="flex-1 px-3 py-2 bg-gray-100 text-gray-600 rounded-xl hover:bg-gray-200 transition text-sm font-medium"
                                    >
                                        Сбросить
                                    </button>
                                    <button
                                        onClick={applyFilters}
                                        className="flex-1 px-3 py-2 bg-orange-500 text-white rounded-xl hover:bg-orange-600 transition text-sm font-medium"
                                    >
                                        Применить
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* Правая часть */}
                <div className="flex-1">
                    {/* Поисковая строка */}
                    <div className="mb-8">
                        <div className="flex flex-wrap gap-2 mb-4">
                            <button
                                onClick={() => setSearchType('startsWith')}
                                className={`px-4 py-1.5 rounded-full text-sm transition ${
                                    searchType === 'startsWith'
                                        ? 'bg-orange-500 text-white'
                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                }`}
                            >
                                С начала номера
                            </button>
                            <button
                                onClick={() => setSearchType('exact')}
                                className={`px-4 py-1.5 rounded-full text-sm transition ${
                                    searchType === 'exact'
                                        ? 'bg-orange-500 text-white'
                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                }`}
                            >
                                Точный номер
                            </button>
                            <button
                                onClick={() => setSearchType('name')}
                                className={`px-4 py-1.5 rounded-full text-sm transition ${
                                    searchType === 'name'
                                        ? 'bg-orange-500 text-white'
                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                }`}
                            >
                                По названию
                            </button>
                        </div>

                        <div className="relative">
                            <input
                                type="text"
                                placeholder={getPlaceholder()}
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                                className="w-full pl-12 pr-28 py-3.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent bg-gray-50"
                            />
                            <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                            <button
                                onClick={handleSearch}
                                className="absolute right-2 top-1/2 transform -translate-y-1/2 px-6 py-1.5 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition text-sm font-medium"
                            >
                                Найти
                            </button>
                        </div>
                    </div>

                    {/* Результаты */}
                    {loading ? (
                        <div className="flex justify-center items-center h-64">
                            <div className="text-gray-400">Загрузка...</div>
                        </div>
                    ) : products.length === 0 && !searchQuery ? (
                        <div className="text-center text-gray-400 py-12">
                            <p className="text-lg mb-2">Нет товаров в этой категории</p>
                            <p className="text-sm">Попробуйте изменить фильтры</p>
                        </div>
                    ) : products.length === 0 && searchQuery ? (
                        <div className="text-center text-gray-400 py-12">
                            <p className="text-lg mb-2">Товары не найдены</p>
                            <p className="text-sm">Попробуйте изменить поисковый запрос или тип поиска</p>
                        </div>
                    ) : (
                        <>
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
                                {products.map(product => (
                                    <ProductCard
                                        key={product.id}
                                        product={product}
                                        onAddToCart={addToCart}
                                    />
                                ))}
                            </div>

                            {totalPages > 1 && !searchQuery && (
                                <div className="flex justify-center gap-3 mt-10">
                                    <button
                                        onClick={() => goToPage(currentPage - 1)}
                                        disabled={currentPage === 0}
                                        className="px-4 py-2 bg-gray-100 text-gray-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200 transition text-sm"
                                    >
                                        ← Назад
                                    </button>
                                    <span className="px-4 py-2 text-gray-500 text-sm">
                                        {currentPage + 1} / {totalPages}
                                    </span>
                                    <button
                                        onClick={() => goToPage(currentPage + 1)}
                                        disabled={currentPage >= totalPages - 1}
                                        className="px-4 py-2 bg-gray-100 text-gray-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200 transition text-sm"
                                    >
                                        Вперед →
                                    </button>
                                </div>
                            )}
                        </>
                    )}

                    {/* Товары от поставщиков */}
                    {(externalProducts.length > 0 || loadingExternal) && (
                        <div className="mt-12 pt-6 border-t border-gray-100">
                            <h2 className="text-lg font-semibold text-orange-500 mb-3">
                                {searchQuery ? 'Товары от поставщиков' : 'Рекомендуемые товары от поставщиков'}
                            </h2>
                            <div className="bg-orange-50/50 rounded-xl p-4 border border-orange-100">
                                <p className="text-sm text-gray-500 mb-3">
                                    {searchQuery
                                        ? 'Эти товары поставляются от наших партнеров. Срок доставки 3-7 дней.'
                                        : 'Возможно эти товары вам подойдут. Срок доставки 3-7 дней.'}
                                </p>
                                {loadingExternal ? (
                                    <div className="text-center py-6 text-gray-400">Загрузка...</div>
                                ) : (
                                    <div className="space-y-3">
                                        {externalProducts.map(product => (
                                            <div key={product.id} className="bg-white rounded-lg p-4 border border-gray-100 shadow-sm">
                                                <div className="flex justify-between items-start flex-wrap gap-4">
                                                    <div className="flex-1">
                                                        <p className="font-semibold text-gray-800">{product.name}</p>
                                                        <p className="text-sm text-gray-500 mt-1">
                                                            {product.producer} | {product.factoryNumber}
                                                        </p>
                                                        <p className="text-xs text-gray-400">Поставщик: {product.supplierName}</p>
                                                    </div>
                                                    <div className="text-right">
                                                        <p className="text-xl font-bold text-orange-500">{product.price.toLocaleString()} ₽</p>
                                                        <p className="text-xs text-gray-500">{product.delivery}</p>
                                                        <button
                                                            onClick={() => addExternalToCart(product)}
                                                            className="mt-2 bg-orange-500 text-white px-4 py-1.5 rounded-lg text-sm hover:bg-orange-600 transition"
                                                        >
                                                            В корзину
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Catalog;
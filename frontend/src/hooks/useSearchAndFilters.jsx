import { useState, useEffect, useCallback } from 'react';
import api from '../api/api';

export function useSearchAndFilters(endpoint, options = {}) {
    const {
        limit = 20,
        defaultFilters = {},
        enableSearch = true,      // можно отключить поиск
        enableFilters = true,     // можно отключить фильтры
        defaultSort = 'id',
        defaultOrder = 'ASC'
    } = options;

    // Состояния
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [totalItems, setTotalItems] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [currentPage, setCurrentPage] = useState(1);

    // Поиск (только если включен)
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearch, setDebouncedSearch] = useState('');

    // Фильтры (только если включены)
    const [filters, setFilters] = useState(defaultFilters);

    // Debounce для поиска
    useEffect(() => {
        if (!enableSearch) return;

        const timer = setTimeout(() => {
            setDebouncedSearch(searchTerm);
            setCurrentPage(1);
        }, 500);

        return () => clearTimeout(timer);
    }, [searchTerm, enableSearch]);

    // Загрузка данных
    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const params = {
                page: currentPage,
                limit: limit,
                sort: defaultSort,
                order: defaultOrder
            };

            // Добавляем поиск
            if (enableSearch && debouncedSearch) {
                params.search = debouncedSearch;
            }

            // Добавляем фильтры (только если включены и есть значения)
            if (enableFilters) {
                Object.keys(filters).forEach(key => {
                    if (filters[key] && filters[key] !== '') {
                        params[key] = filters[key];
                    }
                });
            }

            const response = await api.get(endpoint, { params });

            if (response.data.success) {
                // Поддержка разных форматов ответа
                let responseData;
                if (response.data.data && response.data.data.data) {
                    responseData = response.data.data;
                } else if (response.data.data) {
                    responseData = response.data.data;
                } else {
                    responseData = response.data;
                }

                setData(responseData.data || responseData || []);
                setTotalPages(responseData.totalPages || 1);
                setTotalItems(responseData.total || responseData.length || 0);
            }
        } catch (error) {
            console.error('Ошибка загрузки:', error);
        } finally {
            setLoading(false);
        }
    }, [currentPage, debouncedSearch, filters, endpoint, limit, enableSearch, enableFilters, defaultSort, defaultOrder]);

    useEffect(() => {
        loadData();
    }, [loadData]);

    // Обновление фильтра
    const updateFilter = (key, value) => {
        if (!enableFilters) return;
        setFilters(prev => ({ ...prev, [key]: value }));
        setCurrentPage(1);
    };

    // Сброс всех фильтров
    const clearFilters = () => {
        if (!enableFilters) return;
        setFilters(defaultFilters);
        setCurrentPage(1);
    };

    // Сброс поиска
    const clearSearch = () => {
        if (!enableSearch) return;
        setSearchTerm('');
        setCurrentPage(1);
    };

    // Сброс всего (поиск + фильтры)
    const resetAll = () => {
        if (enableSearch) setSearchTerm('');
        if (enableFilters) setFilters(defaultFilters);
        setCurrentPage(1);
    };

    // Количество активных фильтров
    const getFilterCount = () => {
        if (!enableFilters) return 0;
        return Object.values(filters).filter(v => v && v !== '').length;
    };

    // Переход на страницу
    const goToPage = (page) => {
        if (page >= 1 && page <= totalPages) {
            setCurrentPage(page);
        }
    };

    return {
        // Данные
        data,
        loading,
        totalItems,
        totalPages,
        currentPage,

        // Поиск
        searchTerm,
        setSearchTerm,
        clearSearch,

        // Фильтры
        filters,
        updateFilter,
        clearFilters,
        getFilterCount,

        // Пагинация
        goToPage,
        setCurrentPage,

        // Сброс всего
        resetAll,

        // Перезагрузка
        reload: loadData
    };
}
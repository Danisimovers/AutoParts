import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminReports() {
    const [summary, setSummary] = useState(null);
    const [revenue, setRevenue] = useState([]);
    const [topProducts, setTopProducts] = useState([]);
    const [ordersByStatus, setOrdersByStatus] = useState([]);
    const [period, setPeriod] = useState('all');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadAllReports();
    }, [period]);

    const loadAllReports = async () => {
        setLoading(true);
        await loadSummary();
        await loadRevenue();
        await loadTopProducts();
        await loadOrdersByStatus();
        setLoading(false);
    };

    const loadSummary = async () => {
        try {
            const response = await api.get('/admin/reports/summary');
            if (response.data.success) {
                setSummary(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки статистики:', error);
        }
    };

    const loadRevenue = async () => {
        try {
            const url = period === 'all' ? '/admin/reports/revenue' : `/admin/reports/revenue?period=${period}`;
            const response = await api.get(url);
            if (response.data.success) {
                setRevenue(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки выручки:', error);
        }
    };

    const loadTopProducts = async () => {
        try {
            const response = await api.get('/admin/reports/top-products');
            if (response.data.success) {
                setTopProducts(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки топ товаров:', error);
        }
    };

    const loadOrdersByStatus = async () => {
        try {
            const response = await api.get('/admin/reports/orders-by-status');
            if (response.data.success) {
                setOrdersByStatus(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки статусов:', error);
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'CREATED': return 'Создан';
            case 'PAID': return 'Оплачен';
            case 'SHIPPED': return 'Отправлен';
            case 'DELIVERED': return 'Доставлен';
            case 'CANCELLED': return 'Отменен';
            default: return status;
        }
    };

    const getStatusColor = (count, total) => {
        if (count === 0) return 'bg-gray-100';
        const percentage = (count / total) * 100;
        if (percentage > 30) return 'bg-green-100';
        if (percentage > 10) return 'bg-yellow-100';
        return 'bg-blue-100';
    };

    if (loading) return <div className="p-5">Загрузка...</div>;

    const totalOrdersCount = ordersByStatus.reduce((sum, item) => sum + item.count, 0);

    return (
        <div className="p-5">
            <h1 className="text-3xl font-bold mb-6">Отчеты и аналитика</h1>

            {/* Карточки с общей статистикой */}
            {summary && (
                <div className="grid grid-cols-4 gap-4 mb-8">
                    <div className="bg-white p-4 rounded-lg shadow border">
                        <p className="text-gray-500 text-sm">Общая выручка</p>
                        <p className="text-2xl font-bold text-green-600">{summary.totalRevenue.toLocaleString()} ₽</p>
                    </div>
                    <div className="bg-white p-4 rounded-lg shadow border">
                        <p className="text-gray-500 text-sm">Всего заказов</p>
                        <p className="text-2xl font-bold text-blue-600">{summary.totalOrders}</p>
                    </div>
                    <div className="bg-white p-4 rounded-lg shadow border">
                        <p className="text-gray-500 text-sm">Пользователей</p>
                        <p className="text-2xl font-bold text-purple-600">{summary.totalUsers}</p>
                    </div>
                    <div className="bg-white p-4 rounded-lg shadow border">
                        <p className="text-gray-500 text-sm">Товаров</p>
                        <p className="text-2xl font-bold text-orange-600">{summary.totalProducts}</p>
                    </div>
                </div>
            )}

            {/* Выручка за период */}
            <div className="bg-white p-4 rounded-lg shadow mb-6">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold">Выручка</h2>
                    <select value={period} onChange={(e) => setPeriod(e.target.value)} className="p-2 border rounded">
                        <option value="all">Все время</option>
                        <option value="week">За неделю</option>
                        <option value="month">За месяц</option>
                    </select>
                </div>
                {revenue.length === 0 ? (
                    <p className="text-gray-500">Нет данных</p>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-100">
                            <tr>
                                <th className="p-2 text-left">Дата</th>
                                <th className="p-2 text-right">Выручка</th>
                            </tr>
                            </thead>
                            <tbody>
                            {revenue.map((item, idx) => (
                                <tr key={idx} className="border-b">
                                    <td className="p-2">{item.date}</td>
                                    <td className="p-2 text-right font-bold text-green-600">{item.revenue} ₽</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Топ товаров */}
            <div className="bg-white p-4 rounded-lg shadow mb-6">
                <h2 className="text-xl font-bold mb-4">Топ-10 продаваемых товаров</h2>
                {topProducts.length === 0 ? (
                    <p className="text-gray-500">Нет данных</p>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-100">
                            <tr>
                                <th className="p-2 text-left">Название</th>
                                <th className="p-2 text-left">Артикул</th>
                                <th className="p-2 text-center">Продано, шт</th>
                                <th className="p-2 text-right">Выручка</th>
                            </tr>
                            </thead>
                            <tbody>
                            {topProducts.map((product, idx) => (
                                <tr key={idx} className="border-b">
                                    <td className="p-2">{product.name}</td>
                                    <td className="p-2">{product.sku}</td>
                                    <td className="p-2 text-center font-bold">{product.total_quantity}</td>
                                    <td className="p-2 text-right text-green-600">{product.total_revenue} ₽</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Статусы заказов */}
            <div className="bg-white p-4 rounded-lg shadow">
                <h2 className="text-xl font-bold mb-4">Заказы по статусам</h2>
                {ordersByStatus.length === 0 ? (
                    <p className="text-gray-500">Нет данных</p>
                ) : (
                    <div className="grid grid-cols-5 gap-3">
                        {ordersByStatus.map((item, idx) => (
                            <div key={idx} className={`p-3 rounded-lg text-center ${getStatusColor(item.count, totalOrdersCount)}`}>
                                <p className="text-lg font-bold">{item.count}</p>
                                <p className="text-sm text-gray-600">{getStatusText(item.status)}</p>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminReports;
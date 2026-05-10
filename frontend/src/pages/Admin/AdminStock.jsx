import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminStock() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [stockValues, setStockValues] = useState({});

    useEffect(() => {
        loadProducts();
    }, []);

    const loadProducts = async () => {
        setLoading(true);
        try {
            const response = await api.get('/admin/products');
            if (response.data.success) {
                setProducts(response.data.data);
                const initialStock = {};
                response.data.data.forEach(product => {
                    initialStock[product.id] = product.stock;
                });
                setStockValues(initialStock);
            }
        } catch (error) {
            console.error('Ошибка загрузки товаров:', error);
            alert('Ошибка загрузки товаров');
        } finally {
            setLoading(false);
        }
    };

    const updateStock = async (productId) => {
        const quantity = stockValues[productId];
        if (quantity === undefined || quantity === null) return;

        setUpdating(true);
        try {
            await api.put(`/admin/stock/${productId}?quantity=${quantity}`);
            alert('Остатки обновлены');
            loadProducts();
        } catch (error) {
            console.error('Ошибка обновления остатков:', error);
            alert('Ошибка обновления остатков');
        } finally {
            setUpdating(false);
        }
    };

    const handleStockChange = (productId, value) => {
        setStockValues(prev => ({
            ...prev,
            [productId]: parseInt(value) || 0
        }));
    };

    if (loading) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Управление остатками</h1>
                <p className="text-sm text-gray-500 mt-1">Всего товаров: {products.length}</p>
            </div>

            <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                <table className="w-full">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Артикул</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Название</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Остаток</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Изменить</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600"></th>
                    </tr>
                    </thead>
                    <tbody>
                    {products.map(product => (
                        <tr key={product.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{product.id}</td>
                            <td className="p-3 text-sm font-mono text-gray-600">{product.sku}</td>
                            <td className="p-3 text-sm font-medium text-gray-800">{product.name}</td>
                            <td className="text-center p-3 text-sm font-medium text-gray-800">
                                {product.stock} шт.
                            </td>
                            <td className="text-center p-3">
                                <input
                                    type="number"
                                    value={stockValues[product.id] ?? product.stock}
                                    onChange={(e) => handleStockChange(product.id, e.target.value)}
                                    className="w-28 px-3 py-1.5 text-center border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                                />
                            </td>
                            <td className="text-center p-3">
                                <button
                                    onClick={() => updateStock(product.id)}
                                    disabled={updating}
                                    className="bg-orange-500 text-white px-4 py-1.5 rounded-lg hover:bg-orange-600 transition text-sm disabled:opacity-50"
                                >
                                    Обновить
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default AdminStock;
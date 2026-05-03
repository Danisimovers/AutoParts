import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminStock() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);

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

    const updateStock = async (productId, quantity) => {
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

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    return (
        <div style={{ padding: '20px', flex: 1 }}>
            <h1 style={{ marginBottom: '20px' }}>Управление остатками</h1>

            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>ID</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Артикул</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Название</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Текущий остаток</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Изменить</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Действие</th>
                </tr>
                </thead>
                <tbody>
                {products.map(product => (
                    <tr key={product.id} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '10px' }}>{product.id}</td>
                        <td style={{ padding: '10px' }}>{product.sku}</td>
                        <td style={{ padding: '10px' }}>{product.name}</td>
                        <td style={{ textAlign: 'center', padding: '10px', fontWeight: 'bold' }}>
                            {product.stock} шт.
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <input
                                type="number"
                                id={`stock-${product.id}`}
                                defaultValue={product.stock}
                                style={{ width: '100px', padding: '8px', textAlign: 'center', border: '1px solid #ddd', borderRadius: '4px' }}
                            />
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <button
                                onClick={() => {
                                    const newStock = document.getElementById(`stock-${product.id}`).value;
                                    updateStock(product.id, parseInt(newStock));
                                }}
                                disabled={updating}
                                style={{
                                    padding: '6px 16px',
                                    backgroundColor: '#e67e22',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '4px',
                                    cursor: 'pointer'
                                }}
                            >
                                Обновить
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default AdminStock;
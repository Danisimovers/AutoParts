import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';
import { ShoppingCart, ArrowLeft, Store } from 'lucide-react';

function ProductDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);

    useEffect(() => {
        loadProduct();
    }, [id]);

    const loadProduct = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/products/${id}`);
            if (response.data.success) {
                setProduct(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки товара:', error);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async () => {
        if (!isAuthenticated) {
            alert('Для добавления в корзину необходимо войти');
            navigate('/login');
            return;
        }
        try {
            const response = await api.post('/cart/add', {
                productId: product.id,
                quantity: quantity
            });
            if (response.data.success) {
                alert(`${product.name} добавлен в корзину (${quantity} шт)`);
            }
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Ошибка добавления в корзину');
        }
    };

    if (loading) return (
        <div className="flex justify-center items-center min-h-screen">
            <div className="text-gray-400">Загрузка...</div>
        </div>
    );

    if (!product) return (
        <div className="flex justify-center items-center min-h-screen">
            <div className="text-red-500">Товар не найден</div>
        </div>
    );

    return (
        <div className="max-w-6xl mx-auto px-4 py-8">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 text-gray-500 hover:text-orange-500 transition mb-6"
            >
                <ArrowLeft size={20} />
                <span>Назад</span>
            </button>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Левая колонка */}
                <div className="space-y-6">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-800 mb-2">{product.name}</h1>
                        <div className="flex flex-wrap gap-3 text-sm text-gray-500">
                            <span className="bg-gray-100 px-3 py-1 rounded-full">Артикул: {product.sku}</span>
                            {product.oemCode && (
                                <span className="bg-gray-100 px-3 py-1 rounded-full">OEM: {product.oemCode}</span>
                            )}
                        </div>
                    </div>

                    <div className="border-t pt-4">
                        <h3 className="text-lg font-semibold text-gray-800 mb-2">Описание</h3>
                        <p className="text-gray-600 leading-relaxed">
                            {product.description || 'Описание отсутствует'}
                        </p>
                    </div>

                    {product.compatibleVehicles && product.compatibleVehicles.length > 0 && (
                        <div className="border-t pt-4">
                            <h3 className="text-lg font-semibold text-gray-800 mb-3">Совместимость</h3>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                                {product.compatibleVehicles.map((vehicle, idx) => (
                                    <div key={idx} className="text-sm text-gray-600">
                                        • {vehicle}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="border-t pt-4">
                        <h3 className="text-lg font-semibold text-gray-800 mb-3">Характеристики</h3>
                        <div className="space-y-2 text-sm">
                            <div className="flex">
                                <span className="w-28 text-gray-500">Категория:</span>
                                <span className="text-gray-700">{product.categoryName || '—'}</span>
                            </div>
                            <div className="flex">
                                <span className="w-28 text-gray-500">Производитель:</span>
                                <span className="text-gray-700">{product.manufacturerName || '—'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Правая колонка */}
                <div className="lg:sticky lg:top-4 h-fit">
                    <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-6">
                        <div className="mb-6">
                            <div className="text-4xl font-bold text-orange-500">{product.price} ₽</div>
                            <div className="flex items-center gap-2 mt-2">
                                <div className={`w-2 h-2 rounded-full ${product.stock > 0 ? 'bg-green-500' : 'bg-red-500'}`} />
                                <span className={`text-sm ${product.stock > 0 ? 'text-green-600' : 'text-red-600'}`}>
                                    {product.stock > 0 ? `В наличии: ${product.stock} шт` : 'Нет в наличии'}
                                </span>
                            </div>
                        </div>

                        {/* Самовывоз */}
                        <div className="bg-gray-50 rounded-xl p-4 mb-6">
                            <div className="flex items-center gap-3 mb-2">
                                <Store size={18} className="text-gray-500" />
                                <span className="text-sm font-medium text-gray-700">Самовывоз</span>
                            </div>
                            <p className="text-sm text-gray-500 pl-7">
                                Вы можете забрать товар самостоятельно со склада
                            </p>
                        </div>

                        {product.stock > 0 && (
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-gray-700 mb-2">Количество</label>
                                <div className="flex items-center gap-3">
                                    <button
                                        onClick={() => setQuantity(Math.max(1, quantity - 1))}
                                        className="w-10 h-10 rounded-lg border border-gray-200 flex items-center justify-center hover:border-orange-300 hover:text-orange-500 transition"
                                    >
                                        -
                                    </button>
                                    <input
                                        type="number"
                                        min="1"
                                        max={product.stock}
                                        value={quantity}
                                        onChange={(e) => setQuantity(Math.min(product.stock, Math.max(1, parseInt(e.target.value) || 1)))}
                                        className="w-20 h-10 text-center border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                                    />
                                    <button
                                        onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                                        className="w-10 h-10 rounded-lg border border-gray-200 flex items-center justify-center hover:border-orange-300 hover:text-orange-500 transition"
                                    >
                                        +
                                    </button>
                                    <span className="text-sm text-gray-400 ml-2">
                                        {product.stock} шт доступно
                                    </span>
                                </div>
                            </div>
                        )}

                        <button
                            onClick={addToCart}
                            disabled={product.stock === 0}
                            className={`w-full py-3 rounded-xl font-medium flex items-center justify-center gap-2 transition ${
                                product.stock > 0
                                    ? 'bg-orange-500 text-white hover:bg-orange-600'
                                    : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                            }`}
                        >
                            <ShoppingCart size={18} />
                            {product.stock > 0 ? 'В корзину' : 'Нет в наличии'}
                        </button>

                        {!isAuthenticated && (
                            <p className="text-center text-xs text-gray-400 mt-4">
                                Для оформления заказа необходимо{' '}
                                <button onClick={() => navigate('/login')} className="text-orange-500 hover:underline">
                                    войти
                                </button>
                            </p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProductDetail;
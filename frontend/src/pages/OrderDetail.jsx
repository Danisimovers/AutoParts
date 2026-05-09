import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/api';

function OrderDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadOrder();
    }, [id]);

    const loadOrder = async () => {
        setLoading(true);
        try {
            const orderResponse = await api.get(`/orders/${id}`);
            if (orderResponse.data.success) {
                setOrder(orderResponse.data.data);

                const itemsResponse = await api.get(`/orders/${id}/items`);
                if (itemsResponse.data.success) {
                    setItems(itemsResponse.data.data);
                }
            }
        } catch (error) {
            console.error('Ошибка загрузки заказа:', error);
        } finally {
            setLoading(false);
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

    const getStatusColor = (status) => {
        switch(status) {
            case 'CREATED': return 'bg-yellow-100 text-yellow-800';
            case 'PAID': return 'bg-blue-100 text-blue-800';
            case 'SHIPPED': return 'bg-purple-100 text-purple-800';
            case 'DELIVERED': return 'bg-green-100 text-green-800';
            case 'CANCELLED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    if (loading) return (
        <div className="p-5 text-center text-gray-500">
            Загрузка...
        </div>
    );

    if (!order) return (
        <div className="p-5 text-center text-gray-500">
            Заказ не найден
        </div>
    );

    return (
        <div className="p-5 max-w-[800px] mx-auto">
            <button
                onClick={() => navigate('/my-orders')}
                className="mb-5 px-4 py-2 text-orange-600 hover:text-orange-700 transition flex items-center gap-2"
            >
                ← Назад к заказам
            </button>

            <h1 className="text-2xl font-bold text-gray-800 mb-5">
                Заказ #{order.id}
            </h1>

            <div className="mb-5 p-4 bg-gray-50 rounded-lg">
                <p className="mb-2 text-gray-700">
                    <strong className="font-semibold">Дата:</strong>{' '}
                    {new Date(order.createdAt).toLocaleString('ru-RU')}
                </p>
                <p className="mb-2 text-gray-700">
                    <strong className="font-semibold">Статус:</strong>
                    <span className={`ml-2 px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(order.status)}`}>
                        {getStatusText(order.status)}
                    </span>
                </p>
                <p className="text-gray-700">
                    <strong className="font-semibold">Общая сумма:</strong>{' '}
                    <span className="text-2xl font-bold text-orange-500">
                        {order.total.toLocaleString()} ₽
                    </span>
                </p>
            </div>

            <h2 className="text-xl font-bold text-gray-800 mb-4">
                Товары в заказе
            </h2>

            <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                    <thead>
                    <tr className="border-b-2 border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">
                            Товар
                        </th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">
                            Цена
                        </th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">
                            Количество
                        </th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">
                            Сумма
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    {items.map(item => (
                        <tr key={item.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3">
                                <div>
                                    <strong className="text-gray-800">{item.productName}</strong>
                                    <div className="text-xs text-gray-400 mt-0.5">
                                        Артикул: {item.productSku}
                                    </div>
                                </div>
                            </td>
                            <td className="text-center p-3 text-gray-700">
                                {item.price.toLocaleString()} ₽
                            </td>
                            <td className="text-center p-3 text-gray-700">
                                {item.quantity}
                            </td>
                            <td className="text-center p-3 font-bold text-gray-800">
                                {item.total.toLocaleString()} ₽
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default OrderDetail;
import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { CheckCircle, ShoppingBag } from 'lucide-react';

function OrderSuccess() {
    const location = useLocation();
    const navigate = useNavigate();
    const { order } = location.state || {};

    if (!order) {
        return (
            <div className="min-h-[60vh] flex flex-col items-center justify-center p-5">
                <div className="text-center">
                    <p className="text-gray-500 mb-4">Информация о заказе не найдена</p>
                    <button
                        onClick={() => navigate('/')}
                        className="px-6 py-2.5 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition font-medium"
                    >
                        Перейти в каталог
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-[60vh] flex items-center justify-center p-5">
            <div className="max-w-md w-full">
                {/* Успех */}
                <div className="bg-green-50 text-green-800 p-8 rounded-2xl shadow-sm border border-green-100 text-center">
                    <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-5">
                        <CheckCircle size={40} className="text-green-600" />
                    </div>

                    <h1 className="text-2xl font-bold text-green-800 mb-2">
                        Заказ успешно оформлен!
                    </h1>

                    <div className="bg-white rounded-xl p-5 mb-6 text-left">
                        <div className="flex justify-between py-2 border-b border-gray-100">
                            <span className="text-gray-500">Номер заказа:</span>
                            <span className="font-semibold text-gray-800">#{order.id}</span>
                        </div>
                        <div className="flex justify-between py-2 border-b border-gray-100">
                            <span className="text-gray-500">Сумма заказа:</span>
                            <span className="font-bold text-orange-600 text-lg">{order.total?.toLocaleString()} ₽</span>
                        </div>
                        <div className="flex justify-between py-2">
                            <span className="text-gray-500">Статус:</span>
                            <span className="px-3 py-0.5 bg-yellow-100 text-yellow-700 rounded-full text-sm font-medium">
                                {order.status === 'CREATED' ? 'Создан' : order.status}
                            </span>
                        </div>
                    </div>

                    <div className="flex flex-col sm:flex-row gap-3 justify-center">
                        <button
                            onClick={() => navigate('/')}
                            className="flex items-center justify-center gap-2 px-6 py-2.5 bg-orange-500 text-white rounded-xl hover:bg-orange-600 transition font-medium"
                        >
                            <ShoppingBag size={18} />
                            Продолжить покупки
                        </button>
                        <button
                            onClick={() => navigate('/my-orders')}
                            className="px-6 py-2.5 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition font-medium"
                        >
                            Мои заказы
                        </button>
                    </div>
                </div>

                {/* Дополнительная информация */}
                <div className="mt-6 text-center text-sm text-gray-400">
                    <p>Подтверждение заказа отправлено на вашу почту</p>
                </div>
            </div>
        </div>
    );
}

export default OrderSuccess;
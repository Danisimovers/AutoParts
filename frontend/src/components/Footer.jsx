import React from 'react';
import { Link } from 'react-router-dom';
import { Truck, ShieldCheck, Clock, MapPin, Phone, Mail, Package, Headphones } from 'lucide-react';

function Footer() {
    const currentYear = new Date().getFullYear();

    return (
        <footer className="bg-gray-900 text-gray-300 mt-auto">
            {/* Основные преимущества */}
            <div className="border-b border-gray-800">
                <div className="max-w-7xl mx-auto px-4 py-8">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                        <div className="flex items-center gap-3">
                            <Truck size={32} className="text-orange-500" />
                            <div>
                                <h4 className="font-semibold text-white">Быстрая обработка</h4>
                                <p className="text-sm text-gray-400">Отправка в течение 24 часов</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <ShieldCheck size={32} className="text-orange-500" />
                            <div>
                                <h4 className="font-semibold text-white">Гарантия качества</h4>
                                <p className="text-sm text-gray-400">Оригинальные запчасти</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <Clock size={32} className="text-orange-500" />
                            <div>
                                <h4 className="font-semibold text-white">Работаем для вас</h4>
                                <p className="text-sm text-gray-400">Пн-Вс: 09:00 - 21:00</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <Package size={32} className="text-orange-500" />
                            <div>
                                <h4 className="font-semibold text-white">Большой ассортимент</h4>
                                <p className="text-sm text-gray-400">Детали для всех моделей</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Основное содержимое футера */}
            <div className="max-w-7xl mx-auto px-4 py-10">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                    {/* О компании */}
                    <div>
                        <h3 className="text-white font-bold text-lg mb-4">AutoParts Shop</h3>
                        <p className="text-sm text-gray-400 leading-relaxed">
                            Интернет-магазин автозапчастей для грузовых автомобилей.
                            Широкий ассортимент оригинальных деталей от ведущих производителей.
                        </p>
                    </div>

                    {/* Навигация */}
                    <div>
                        <h3 className="text-white font-semibold mb-4">Меню</h3>
                        <ul className="space-y-2 text-sm">
                            <li><Link to="/" className="text-gray-400 hover:text-orange-500 transition">Каталог</Link></li>
                            <li><Link to="/cart" className="text-gray-400 hover:text-orange-500 transition">Корзина</Link></li>
                            <li><Link to="/my-orders" className="text-gray-400 hover:text-orange-500 transition">Мои заказы</Link></li>
                            <li><Link to="/profile" className="text-gray-400 hover:text-orange-500 transition">Личный кабинет</Link></li>
                        </ul>
                    </div>

                    {/* Контакты */}
                    <div>
                        <h3 className="text-white font-semibold mb-4">Контакты</h3>
                        <ul className="space-y-3 text-sm">
                            <li className="flex items-center gap-2">
                                <Phone size={16} className="text-gray-400" />
                                <span className="text-gray-400">8 (800) 123-45-67</span>
                            </li>
                            <li className="flex items-center gap-2">
                                <Mail size={16} className="text-gray-400" />
                                <span className="text-gray-400">info@autoparts-shop.ru</span>
                            </li>
                            <li className="flex items-start gap-2">
                                <MapPin size={16} className="text-gray-400 mt-0.5" />
                                <span className="text-gray-400">г. Москва, ул. Автозаводская, д. 15, склад 3</span>
                            </li>
                        </ul>
                    </div>

                    {/* График работы */}
                    <div>
                        <h3 className="text-white font-semibold mb-4">График работы</h3>
                        <ul className="space-y-2 text-sm">
                            <li className="flex justify-between">
                                <span className="text-gray-400">Понедельник - Пятница:</span>
                                <span className="text-gray-300">09:00 - 20:00</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-gray-400">Суббота:</span>
                                <span className="text-gray-300">10:00 - 18:00</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-gray-400">Воскресенье:</span>
                                <span className="text-gray-300">10:00 - 16:00</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            {/* Нижняя часть */}
            <div className="border-t border-gray-800">
                <div className="max-w-7xl mx-auto px-4 py-6">
                    <div className="flex flex-col md:flex-row justify-between items-center gap-4 text-sm">
                        <p className="text-gray-400">
                            © {currentYear} AutoParts Shop. Все права защищены.
                        </p>
                        <div className="flex gap-6">
                            <Link to="/" className="text-gray-400 hover:text-orange-500 transition">Политика конфиденциальности</Link>
                            <Link to="/" className="text-gray-400 hover:text-orange-500 transition">Пользовательское соглашение</Link>
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    );
}

export default Footer;
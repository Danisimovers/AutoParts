import React from 'react';
import { Link } from 'react-router-dom';
import { Truck, ShieldCheck, Clock, MapPin, Phone, Mail, Package } from 'lucide-react';

function Footer() {
    const currentYear = new Date().getFullYear();

    const openMap = () => {
        const lat = 47.674407;
        const lon = 40.066677;
        window.open(`https://yandex.ru/maps/?ll=${lon},${lat}&z=18&pt=${lon},${lat}`, '_blank');
    };

    return (
        <footer className="bg-gray-800 text-gray-300 mt-auto">
            <div className="border-b border-gray-700">
                <div className="max-w-7xl mx-auto px-4 py-8">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                        <div className="flex items-center gap-3">
                            <Truck size={32} className="text-gray-400" />
                            <div>
                                <h4 className="font-semibold text-white">Самовывоз со склада</h4>
                                <p className="text-sm text-gray-400">Бесплатно в любой день</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <ShieldCheck size={32} className="text-gray-400" />
                            <div>
                                <h4 className="font-semibold text-white">Гарантия качества</h4>
                                <p className="text-sm text-gray-400">Оригинальные запчасти</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <Clock size={32} className="text-gray-400" />
                            <div>
                                <h4 className="font-semibold text-white">Работаем для вас</h4>
                                <p className="text-sm text-gray-400">Пн-Вс: 09:00 - 23:00</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <Package size={32} className="text-gray-400" />
                            <div>
                                <h4 className="font-semibold text-white">Большой ассортимент</h4>
                                <p className="text-sm text-gray-400">Детали для всех моделей</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-4 py-10">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                    <div>
                        <h3 className="text-white font-bold text-lg mb-4">AutoParts Shop</h3>
                        <p className="text-sm text-gray-400 leading-relaxed">
                            Интернет-магазин автозапчастей для грузовых автомобилей.
                            Широкий ассортимент оригинальных деталей от ведущих производителей.
                        </p>
                    </div>

                    <div>
                        <h3 className="text-white font-semibold mb-4">Меню</h3>
                        <ul className="space-y-2 text-sm">
                            <li><Link to="/" className="text-gray-400 hover:text-white transition">Каталог</Link></li>
                            <li><Link to="/cart" className="text-gray-400 hover:text-white transition">Корзина</Link></li>
                            <li><Link to="/my-orders" className="text-gray-400 hover:text-white transition">Мои заказы</Link></li>
                            <li><Link to="/profile" className="text-gray-400 hover:text-white transition">Личный кабинет</Link></li>
                        </ul>
                    </div>

                    <div>
                        <h3 className="text-white font-semibold mb-4">Контакты</h3>
                        <ul className="space-y-3 text-sm">
                            <li className="flex items-center gap-2">
                                <Phone size={16} className="text-gray-400" />
                                <a href="tel:89198880127" className="text-gray-400 hover:text-white transition">8 (919) 888-01-27</a>
                            </li>
                            <li className="flex items-center gap-2">
                                <Mail size={16} className="text-gray-400" />
                                <a href="mailto:kazauto1005@mail.ru" className="text-gray-400 hover:text-white transition">kazauto1005@mail.ru</a>
                            </li>
                            <li className="flex items-start gap-2">
                                <MapPin size={16} className="text-gray-400 mt-0.5 cursor-pointer hover:text-white transition" onClick={openMap} />
                                <button onClick={openMap} className="text-gray-400 hover:text-white transition text-left">
                                    Автозапчасти, Ростовская область, Октябрьский район
                                </button>
                            </li>
                        </ul>
                    </div>

                    <div>
                        <h3 className="text-white font-semibold mb-4">График работы</h3>
                        <ul className="space-y-2 text-sm">
                            <li className="flex justify-between">
                                <span className="text-gray-400">Ежедневно:</span>
                                <span className="text-white">09:00 - 23:00</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-gray-400">Без выходных</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <div className="border-t border-gray-700">
                <div className="max-w-7xl mx-auto px-4 py-6">
                    <div className="flex flex-col md:flex-row justify-between items-center gap-4 text-sm">
                        <p className="text-gray-400">
                            © {currentYear} AutoParts Shop. Все права защищены.
                        </p>
                        <div className="flex gap-6">
                            <Link to="/privacy-policy" className="text-gray-400 hover:text-white transition">Политика конфиденциальности</Link>
                            <Link to="/terms" className="text-gray-400 hover:text-white transition">Пользовательское соглашение</Link>
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    );
}

export default Footer;
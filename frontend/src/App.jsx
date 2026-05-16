import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Catalog from './pages/Catalog';
import Cart from './pages/Cart';
import ProductDetail from './pages/ProductDetail';
import OrderSuccess from './pages/OrderSuccess';
import Login from './pages/Login';
import Register from './pages/Register';
import MyOrders from './pages/MyOrders';
import OrderDetail from './pages/OrderDetail';
import VerifyEmail from './pages/VerifyEmail';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Profile from './pages/Profile';
import VinRequestForm from './components/VinRequestForm';
import NotificationBell from './components/NotificationBell';
import UserVinRequestChat from './pages/UserVinRequestChat';
import Footer from './components/Footer';
import PrivacyPolicy from './pages/PrivacyPolicy';
import TermsOfService from './pages/TermsOfService';
import MyRequests from './pages/MyRequests';
import MyVehicles from './pages/MyVehicles';
import AdminVehicles from './pages/Admin/AdminVehicles';


// ADMIN
import AdminLayout from './pages/Admin/AdminLayout';
import AdminProducts from './pages/Admin/AdminProducts';
import AdminUsers from './pages/Admin/AdminUsers';
import AdminStock from './pages/Admin/AdminStock';
import AdminSuppliers from './pages/Admin/AdminSuppliers';
import AdminCategories from './pages/Admin/AdminCategories';
import AdminManufacturers from './pages/Admin/AdminManufacturers';
import AdminReports from './pages/Admin/AdminReports';
import AdminExternalRequests from './pages/Admin/AdminExternalRequests';

// MANAGER
import ManagerLayout from './pages/Manager/ManagerLayout';
import VinRequests from './pages/Manager/VinRequests';
import VinRequestChat from './pages/Manager/VinRequestChat';
import ManagerOrders from './pages/Manager/ManagerOrders';
import ManagerReturns from './pages/Manager/ManagerReturns';
import ManagerExternalRequests from './pages/Manager/ManagerExternalRequests';

function NavBar({ showVinForm, setShowVinForm }) {
    const { user, logout, isAuthenticated } = useAuth();
    const [showDropdown, setShowDropdown] = useState(false);

    React.useEffect(() => {
        const handleClickOutside = (event) => {
            if (showDropdown && !event.target.closest('.dropdown-container')) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('click', handleClickOutside);
        return () => document.removeEventListener('click', handleClickOutside);
    }, [showDropdown]);

    return (
        <nav className="bg-gray-800 shadow-lg sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 py-3 flex justify-between items-center">
                <div className="flex items-center gap-6">
                    <Link to="/" className="text-white font-medium hover:text-gray-300 transition">Каталог</Link>
                    <Link to="/cart" className="text-white font-medium hover:text-gray-300 transition">Корзина</Link>
                    {isAuthenticated && (
                        <Link to="/my-orders" className="text-white font-medium hover:text-gray-300 transition">Мои заказы</Link>
                    )}
                    {isAuthenticated && (
                        <button
                            onClick={() => setShowVinForm(true)}
                            className="text-white font-medium hover:text-gray-300 transition"
                        >
                            Запрос по VIN
                        </button>
                    )}
                    {isAuthenticated && (
                        <Link to="/my-vehicles" className="text-white font-medium hover:text-gray-300 transition">
                            Мои автомобили
                        </Link>
                    )}
                </div>

                <div className="dropdown-container flex items-center gap-4 relative">
                    {isAuthenticated ? (
                        <>
                            <NotificationBell />
                            <span className="text-gray-200">Здравствуйте, {user?.login}</span>
                            <button
                                onClick={() => setShowDropdown(!showDropdown)}
                                className="bg-orange-500 text-white rounded-full w-9 h-9 flex items-center justify-center font-bold hover:bg-orange-600 transition shadow-md"
                            >
                                {user?.login?.charAt(0).toUpperCase()}
                            </button>

                            {showDropdown && (
                                <div className="absolute top-12 right-0 bg-white rounded-xl shadow-2xl w-48 overflow-hidden z-50 border border-gray-100">
                                    <Link
                                        to="/profile"
                                        onClick={() => setShowDropdown(false)}
                                        className="block px-4 py-3 text-gray-700 hover:bg-gray-50 transition"
                                    >
                                        Личный кабинет
                                    </Link>
                                    <Link
                                        to="/my-requests"
                                        onClick={() => setShowDropdown(false)}
                                        className="block px-4 py-3 text-gray-700 hover:bg-gray-50 transition border-t border-gray-100"
                                    >
                                        Мои заявки
                                    </Link>
                                    {(user?.role === 'MANAGER' || user?.role === 'ADMIN') && (
                                        <Link
                                            to="/manager/vin-requests"
                                            onClick={() => setShowDropdown(false)}
                                            className="block px-4 py-3 text-gray-700 hover:bg-gray-50 transition border-t border-gray-100"
                                        >
                                            Менеджер-панель
                                        </Link>
                                    )}
                                    {user?.role === 'ADMIN' && (
                                        <Link
                                            to="/admin/products"
                                            onClick={() => setShowDropdown(false)}
                                            className="block px-4 py-3 text-gray-700 hover:bg-gray-50 transition border-t border-gray-100"
                                        >
                                            Админ-панель
                                        </Link>
                                    )}
                                    <button
                                        onClick={() => {
                                            setShowDropdown(false);
                                            logout();
                                        }}
                                        className="block w-full text-left px-4 py-3 text-red-600 hover:bg-red-50 transition border-t border-gray-100"
                                    >
                                        Выйти
                                    </button>
                                </div>
                            )}
                        </>
                    ) : (
                        <div className="flex gap-4">
                            <Link to="/login" className="bg-orange-500 text-white px-4 py-1.5 rounded-lg hover:bg-orange-600 transition">Вход</Link>
                            <Link to="/register" className="bg-orange-500 text-white px-4 py-1.5 rounded-lg hover:bg-orange-600 transition">Регистрация</Link>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
}

function AppRoutes() {
    const { isAuthenticated, user } = useAuth();

    return (
        <Routes>
            <Route path="/" element={<Catalog />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/product/:id" element={<ProductDetail />} />
            <Route path="/order-success" element={<OrderSuccess />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/verify" element={<VerifyEmail />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/privacy-policy" element={<PrivacyPolicy />} />
            <Route path="/terms" element={<TermsOfService />} />

            <Route path="/profile" element={isAuthenticated ? <Profile /> : <Navigate to="/login" />} />
            <Route path="/my-requests" element={isAuthenticated ? <MyRequests /> : <Navigate to="/login" />} />
            <Route path="/my-orders" element={isAuthenticated ? <MyOrders /> : <Navigate to="/login" />} />
            <Route path="/my-vehicles" element={isAuthenticated ? <MyVehicles /> : <Navigate to="/login" />} />
            <Route path="/order/:id" element={isAuthenticated ? <OrderDetail /> : <Navigate to="/login" />} />
            <Route path="/vin-requests/:id" element={isAuthenticated ? <UserVinRequestChat /> : <Navigate to="/login" />} />

            <Route path="/admin" element={isAuthenticated && user?.role === 'ADMIN' ? <AdminLayout /> : <Navigate to="/" />}>
                <Route path="products" element={<AdminProducts />} />
                <Route path="users" element={<AdminUsers />} />
                <Route path="stock" element={<AdminStock />} />
                <Route path="suppliers" element={<AdminSuppliers />} />
                <Route path="categories" element={<AdminCategories />} />
                <Route path="manufacturers" element={<AdminManufacturers />} />
                <Route path="reports" element={<AdminReports />} />
                <Route path="external-requests" element={<AdminExternalRequests />} />
                <Route path="vehicles" element={<AdminVehicles />} />
            </Route>

            <Route path="/manager" element={isAuthenticated && (user?.role === 'MANAGER' || user?.role === 'ADMIN') ? <ManagerLayout /> : <Navigate to="/" />}>
                <Route path="vin-requests" element={<VinRequests />} />
                <Route path="vin-requests/:id" element={<VinRequestChat />} />
                <Route path="orders" element={<ManagerOrders />} />
                <Route path="returns" element={<ManagerReturns />} />
                <Route path="external-requests" element={<ManagerExternalRequests />} />
            </Route>
        </Routes>
    );
}

function App() {
    const [showVinForm, setShowVinForm] = useState(false);

    return (
        <BrowserRouter>
            <AuthProvider>
                <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
                    <NavBar showVinForm={showVinForm} setShowVinForm={setShowVinForm} />
                    <div style={{ flex: 1 }}>
                        <AppRoutes />
                    </div>
                    {showVinForm && <VinRequestForm onClose={() => setShowVinForm(false)} />}
                    <Footer />
                </div>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
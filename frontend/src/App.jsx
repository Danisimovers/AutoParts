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


// ADMIN
import AdminLayout from './pages/Admin/AdminLayout';
import AdminProducts from './pages/Admin/AdminProducts';
import AdminUsers from './pages/Admin/AdminUsers';
import AdminStock from './pages/Admin/AdminStock';
import AdminSuppliers from './pages/Admin/AdminSuppliers';
import AdminCategories from './pages/Admin/AdminCategories';




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

    // Закрываем меню при клике вне
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
        <nav style={{
            backgroundColor: '#333',
            padding: '15px 20px',
            display: 'flex',
            gap: '20px',
            justifyContent: 'space-between',
            alignItems: 'center'
        }}>
            {/* Левая часть - основные ссылки */}
            <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                <Link to="/" style={{ color: 'white', textDecoration: 'none' }}>Каталог</Link>
                <Link to="/cart" style={{ color: 'white', textDecoration: 'none' }}>Корзина</Link>
                {isAuthenticated && (
                    <Link to="/my-orders" style={{ color: 'white', textDecoration: 'none' }}>Мои заказы</Link>
                )}
                {isAuthenticated && (
                    <button
                        onClick={() => setShowVinForm(true)}
                        style={{
                            color: 'white',
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '16px'
                        }}
                    >
                        Запрос по VIN
                    </button>
                )}
            </div>

            {/* Правая часть - профиль */}
            <div className="dropdown-container" style={{ display: 'flex', alignItems: 'center', gap: '15px', position: 'relative' }}>
                {isAuthenticated ? (
                    <>
                        <NotificationBell />
                        <span style={{ color: 'white' }}>Здравствуйте, {user?.login}</span>
                        <button
                            onClick={() => setShowDropdown(!showDropdown)}
                            style={{
                                backgroundColor: '#e67e22',
                                color: 'white',
                                border: 'none',
                                borderRadius: '50%',
                                width: '36px',
                                height: '36px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center'
                            }}
                        >
                            {user?.login?.charAt(0).toUpperCase()}
                        </button>

                        {showDropdown && (
                            <div style={{
                                position: 'absolute',
                                top: '45px',
                                right: '0',
                                backgroundColor: 'white',
                                borderRadius: '8px',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                                minWidth: '220px',
                                zIndex: 100,
                                overflow: 'hidden'
                            }}>
                                <Link
                                    to="/profile"
                                    onClick={() => setShowDropdown(false)}
                                    style={{
                                        display: 'block',
                                        padding: '12px 20px',
                                        color: '#333',
                                        textDecoration: 'none',
                                        borderBottom: '1px solid #eee',
                                        fontSize: '14px'
                                    }}
                                    onMouseEnter={(e) => e.target.style.backgroundColor = '#f5f5f5'}
                                    onMouseLeave={(e) => e.target.style.backgroundColor = 'white'}
                                >
                                    Личный кабинет
                                </Link>

                                {/* Менеджер-панель (для MANAGER и ADMIN) */}
                                {(user?.role === 'MANAGER' || user?.role === 'ADMIN') && (
                                    <Link
                                        to="/manager/vin-requests"
                                        onClick={() => setShowDropdown(false)}
                                        style={{
                                            display: 'block',
                                            padding: '12px 20px',
                                            color: '#333',
                                            textDecoration: 'none',
                                            borderBottom: '1px solid #eee',
                                            fontSize: '14px'
                                        }}
                                        onMouseEnter={(e) => e.target.style.backgroundColor = '#f5f5f5'}
                                        onMouseLeave={(e) => e.target.style.backgroundColor = 'white'}
                                    >
                                        Менеджер-панель
                                    </Link>
                                )}

                                {/* Админ-панель (только для ADMIN) */}
                                {user?.role === 'ADMIN' && (
                                    <Link
                                        to="/admin/products"
                                        onClick={() => setShowDropdown(false)}
                                        style={{
                                            display: 'block',
                                            padding: '12px 20px',
                                            color: '#333',
                                            textDecoration: 'none',
                                            borderBottom: '1px solid #eee',
                                            fontSize: '14px'
                                        }}
                                        onMouseEnter={(e) => e.target.style.backgroundColor = '#f5f5f5'}
                                        onMouseLeave={(e) => e.target.style.backgroundColor = 'white'}
                                    >
                                        Админ-панель
                                    </Link>
                                )}

                                <button
                                    onClick={() => {
                                        setShowDropdown(false);
                                        logout();
                                    }}
                                    style={{
                                        display: 'block',
                                        width: '100%',
                                        textAlign: 'left',
                                        padding: '12px 20px',
                                        color: '#f44336',
                                        background: 'none',
                                        border: 'none',
                                        cursor: 'pointer',
                                        fontSize: '14px'
                                    }}
                                    onMouseEnter={(e) => e.target.style.backgroundColor = '#f5f5f5'}
                                    onMouseLeave={(e) => e.target.style.backgroundColor = 'white'}
                                >
                                    Выйти
                                </button>
                            </div>
                        )}
                    </>
                ) : (
                    <div style={{ display: 'flex', gap: '15px' }}>
                        <Link to="/login" style={{ color: 'white', textDecoration: 'none' }}>Вход</Link>
                        <Link to="/register" style={{ color: 'white', textDecoration: 'none' }}>Регистрация</Link>
                    </div>
                )}
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

            <Route path="/profile" element={isAuthenticated ? <Profile /> : <Navigate to="/login" />} />
            <Route path="/my-orders" element={isAuthenticated ? <MyOrders /> : <Navigate to="/login" />} />
            <Route path="/order/:id" element={isAuthenticated ? <OrderDetail /> : <Navigate to="/login" />} />
            <Route path="/vin-requests/:id" element={isAuthenticated ? <UserVinRequestChat /> : <Navigate to="/login" />} />

            {/* Админ-панель */}
            <Route path="/admin" element={isAuthenticated && user?.role === 'ADMIN' ? <AdminLayout /> : <Navigate to="/" />}>
                <Route path="products" element={<AdminProducts />} />
                <Route path="users" element={<AdminUsers />} />
                <Route path="stock" element={<AdminStock />} />
                <Route path="suppliers" element={<AdminSuppliers />} />
                <Route path="categories" element={<AdminCategories />} />
            </Route>

            {/* Менеджер-панель (доступна для MANAGER и ADMIN) */}
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
                <NavBar showVinForm={showVinForm} setShowVinForm={setShowVinForm} />
                <AppRoutes />
                {showVinForm && <VinRequestForm onClose={() => setShowVinForm(false)} />}
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
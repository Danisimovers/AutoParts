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

// ADMIN
import AdminLayout from './pages/Admin/AdminLayout';
import AdminProducts from './pages/Admin/AdminProducts';
import AdminUsers from './pages/Admin/AdminUsers';
import AdminStock from './pages/Admin/AdminStock';

// MANAGER
import ManagerLayout from './pages/Manager/ManagerLayout';
import VinRequests from './pages/Manager/VinRequests';
import ManagerOrders from './pages/Manager/ManagerOrders';

function NavBar({ showVinForm, setShowVinForm }) {
    const { user, logout, isAuthenticated } = useAuth();
    const [showDropdown, setShowDropdown] = useState(false);

    return (
        <nav style={{
            backgroundColor: '#333',
            padding: '15px',
            display: 'flex',
            gap: '20px',
            justifyContent: 'space-between',
            alignItems: 'center'
        }}>
            <div style={{ display: 'flex', gap: '20px' }}>
                <Link to="/" style={{ color: 'white', textDecoration: 'none' }}>Каталог</Link>
                <Link to="/cart" style={{ color: 'white', textDecoration: 'none' }}>Корзина</Link>
                {isAuthenticated && (
                    <>
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
                    </>
                )}
                {isAuthenticated && (user?.role === 'MANAGER' || user?.role === 'ADMIN') && (
                    <Link to="/manager/vin-requests" style={{ color: 'white', textDecoration: 'none' }}>Менеджер</Link>
                )}
                {isAuthenticated && user?.role === 'ADMIN' && (
                    <Link to="/admin/products" style={{ color: 'white', textDecoration: 'none' }}>Админ-панель</Link>
                )}
            </div>

            <div style={{ position: 'relative' }}>
                {isAuthenticated ? (
                    <>
                        <button
                            onClick={() => setShowDropdown(!showDropdown)}
                            style={{
                                backgroundColor: '#555',
                                color: 'white',
                                border: 'none',
                                borderRadius: '50%',
                                width: '40px',
                                height: '40px',
                                fontSize: '18px',
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
                                top: '50px',
                                right: '0',
                                backgroundColor: 'white',
                                borderRadius: '8px',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                                minWidth: '180px',
                                zIndex: 100
                            }}>
                                <Link
                                    to="/profile"
                                    onClick={() => setShowDropdown(false)}
                                    style={{
                                        display: 'block',
                                        padding: '10px 20px',
                                        color: '#333',
                                        textDecoration: 'none',
                                        borderBottom: '1px solid #eee'
                                    }}
                                >
                                    👤 Личный кабинет
                                </Link>
                                <Link
                                    to="/my-orders"
                                    onClick={() => setShowDropdown(false)}
                                    style={{
                                        display: 'block',
                                        padding: '10px 20px',
                                        color: '#333',
                                        textDecoration: 'none',
                                        borderBottom: '1px solid #eee'
                                    }}
                                >
                                    📦 Мои заказы
                                </Link>
                                <button
                                    onClick={() => {
                                        setShowDropdown(false);
                                        logout();
                                    }}
                                    style={{
                                        display: 'block',
                                        width: '100%',
                                        textAlign: 'left',
                                        padding: '10px 20px',
                                        color: '#f44336',
                                        background: 'none',
                                        border: 'none',
                                        cursor: 'pointer',
                                        fontSize: '14px'
                                    }}
                                >
                                    🚪 Выйти
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

            {/* Защищенные маршруты */}
            <Route path="/profile" element={isAuthenticated ? <Profile /> : <Navigate to="/login" />} />
            <Route path="/my-orders" element={isAuthenticated ? <MyOrders /> : <Navigate to="/login" />} />
            <Route path="/order/:id" element={isAuthenticated ? <OrderDetail /> : <Navigate to="/login" />} />

            {/* Админ-панель */}
            <Route path="/admin" element={isAuthenticated && user?.role === 'ADMIN' ? <AdminLayout /> : <Navigate to="/" />}>
                <Route path="products" element={<AdminProducts />} />
                <Route path="users" element={<AdminUsers />} />
                <Route path="stock" element={<AdminStock />} />
            </Route>

            {/* Менеджер-панель */}
            <Route path="/manager" element={isAuthenticated && (user?.role === 'MANAGER' || user?.role === 'ADMIN') ? <ManagerLayout /> : <Navigate to="/" />}>
                <Route path="vin-requests" element={<VinRequests />} />
                <Route path="orders" element={<ManagerOrders />} />
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
import React from 'react';
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

// ADMIN
import AdminLayout from './pages/Admin/AdminLayout';
import AdminProducts from './pages/Admin/AdminProducts';
import AdminUsers from './pages/Admin/AdminUsers';
import AdminStock from './pages/Admin/AdminStock';

// MANAGER
import ManagerLayout from './pages/Manager/ManagerLayout';
import VinRequests from './pages/Manager/VinRequests';
import ManagerOrders from './pages/Manager/ManagerOrders';

function NavBar() {
    const { user, logout, isAuthenticated } = useAuth();

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
                    <Link to="/my-orders" style={{ color: 'white', textDecoration: 'none' }}>Мои заказы</Link>
                )}
                {isAuthenticated && (user?.role === 'MANAGER' || user?.role === 'ADMIN') && (
                    <Link to="/manager/vin-requests" style={{ color: 'white', textDecoration: 'none' }}>Менеджер</Link>
                )}
                {isAuthenticated && user?.role === 'ADMIN' && (
                    <Link to="/admin/products" style={{ color: 'white', textDecoration: 'none' }}>Админ-панель</Link>
                )}
            </div>

            <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                {isAuthenticated ? (
                    <>
                        <span style={{ color: 'white' }}>Привет, {user?.login}</span>
                        <button
                            onClick={logout}
                            style={{
                                backgroundColor: '#ff4444',
                                color: 'white',
                                border: 'none',
                                padding: '5px 15px',
                                borderRadius: '4px',
                                cursor: 'pointer'
                            }}
                        >
                            Выйти
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login" style={{ color: 'white', textDecoration: 'none' }}>Вход</Link>
                        <Link to="/register" style={{ color: 'white', textDecoration: 'none' }}>Регистрация</Link>
                    </>
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
            <Route path="/my-orders" element={isAuthenticated ? <MyOrders /> : <Navigate to="/login" />} />
            <Route path="/order/:id" element={isAuthenticated ? <OrderDetail /> : <Navigate to="/login" />} />
            <Route path="/verify" element={<VerifyEmail />} />

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
    return (
        <BrowserRouter>
            <AuthProvider>
                <NavBar />
                <AppRoutes />
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
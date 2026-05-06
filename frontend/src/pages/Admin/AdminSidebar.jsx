import React from 'react';
import { NavLink } from 'react-router-dom';

function AdminSidebar() {
    const menuItems = [
        { path: '/admin/products', name: 'Товары' },
        { path: '/admin/users', name: 'Пользователи' },
        { path: '/admin/stock', name: 'Остатки' },
        { path: '/admin/returns', name: 'Возвраты' },
        { path: '/admin/suppliers', name: 'Поставщики' },
    ];

    return (
        <div style={{
            width: '260px',
            backgroundColor: '#2c3e50',
            minHeight: 'calc(100vh - 70px)',
            padding: '20px 0',
            position: 'sticky',
            top: 0,
        }}>
            <h3 style={{ color: 'white', textAlign: 'center', marginBottom: '20px' }}>
                Админ-панель
            </h3>
            <nav>
                {menuItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        style={({ isActive }) => ({
                            display: 'block',
                            padding: '12px 20px',
                            color: 'white',
                            textDecoration: 'none',
                            backgroundColor: isActive ? '#e67e22' : 'transparent',
                            transition: '0.3s',
                        })}
                    >
                        {item.name}
                    </NavLink>
                ))}
            </nav>
        </div>
    );
}

export default AdminSidebar;
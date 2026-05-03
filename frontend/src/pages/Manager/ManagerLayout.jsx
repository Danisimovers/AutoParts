import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';

function ManagerLayout() {
    const menuItems = [
        { path: '/manager/vin-requests', name: 'Заявки по VIN' },
        { path: '/manager/orders', name: 'Заказы' },
    ];

    return (
        <div style={{ display: 'flex' }}>
            <div style={{
                width: '260px',
                backgroundColor: '#2c3e50',
                minHeight: 'calc(100vh - 70px)',
                padding: '20px 0',
            }}>
                <h3 style={{ color: 'white', textAlign: 'center', marginBottom: '20px' }}>
                    Менеджер-панель
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
                            })}
                        >
                            {item.name}
                        </NavLink>
                    ))}
                </nav>
            </div>
            <div style={{ flex: 1, padding: '20px' }}>
                <Outlet />
            </div>
        </div>
    );
}

export default ManagerLayout;
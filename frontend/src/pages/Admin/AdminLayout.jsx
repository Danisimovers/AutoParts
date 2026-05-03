import React from 'react';
import { Outlet } from 'react-router-dom';
import AdminSidebar from './AdminSidebar';

function AdminLayout() {
    return (
        <div style={{ display: 'flex' }}>
            <AdminSidebar />
            <div style={{ flex: 1 }}>
                <Outlet />
            </div>
        </div>
    );
}

export default AdminLayout;
import React, { useState, useEffect } from 'react';
import api from '../../api/api';

function AdminUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const response = await api.get('/admin/users');
            if (response.data.success) {
                setUsers(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки пользователей:', error);
            alert('Ошибка загрузки пользователей');
        } finally {
            setLoading(false);
        }
    };

    const changeRole = async (userId, newRole) => {
        try {
            await api.put(`/admin/users/${userId}/role?role=${newRole}`);
            alert('Роль пользователя изменена');
            loadUsers();
        } catch (error) {
            console.error('Ошибка изменения роли:', error);
            alert('Ошибка изменения роли');
        }
    };

    const getRoleColor = (role) => {
        switch(role) {
            case 'ADMIN': return '#e67e22';
            case 'MANAGER': return '#2196f3';
            default: return '#666';
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    return (
        <div style={{ padding: '20px', flex: 1 }}>
            <h1 style={{ marginBottom: '20px' }}>Управление пользователями</h1>

            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>ID</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Логин</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Email</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Телефон</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Роль</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Действия</th>
                </tr>
                </thead>
                <tbody>
                {users.map(user => (
                    <tr key={user.id} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '10px' }}>{user.id}</td>
                        <td style={{ padding: '10px' }}>{user.login}</td>
                        <td style={{ padding: '10px' }}>{user.email || '-'}</td>
                        <td style={{ padding: '10px' }}>{user.phone || '-'}</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                                <span style={{
                                    backgroundColor: getRoleColor(user.role),
                                    color: 'white',
                                    padding: '4px 12px',
                                    borderRadius: '20px',
                                    fontSize: '12px'
                                }}>
                                    {user.role}
                                </span>
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            {user.role !== 'ADMIN' && (
                                <select
                                    onChange={(e) => changeRole(user.id, e.target.value)}
                                    defaultValue={user.role}
                                    style={{ padding: '5px 10px', borderRadius: '4px', border: '1px solid #ddd' }}
                                >
                                    <option value="CUSTOMER">Покупатель</option>
                                    <option value="MANAGER">Менеджер</option>
                                    <option value="ADMIN">Админ</option>
                                </select>
                            )}
                            {user.role === 'ADMIN' && <span style={{ color: '#666' }}>Нельзя изменить</span>}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default AdminUsers;
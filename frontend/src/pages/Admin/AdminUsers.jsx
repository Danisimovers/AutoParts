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
            case 'ADMIN': return 'bg-orange-100 text-orange-700';
            case 'MANAGER': return 'bg-blue-100 text-blue-700';
            default: return 'bg-gray-100 text-gray-700';
        }
    };

    const getRoleText = (role) => {
        switch(role) {
            case 'ADMIN': return 'Админ';
            case 'MANAGER': return 'Менеджер';
            default: return 'Покупатель';
        }
    };

    if (loading) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Управление пользователями</h1>
                <p className="text-sm text-gray-500 mt-1">Всего пользователей: {users.length}</p>
            </div>

            <div className="overflow-x-auto bg-white rounded-xl border border-gray-200">
                <table className="w-full">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">ID</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Логин</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Email</th>
                        <th className="text-left p-3 text-sm font-semibold text-gray-600">Телефон</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Роль</th>
                        <th className="text-center p-3 text-sm font-semibold text-gray-600">Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map(user => (
                        <tr key={user.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                            <td className="p-3 text-sm text-gray-500">{user.id}</td>
                            <td className="p-3 text-sm font-medium text-gray-800">{user.login}</td>
                            <td className="p-3 text-sm text-gray-600">{user.email || '-'}</td>
                            <td className="p-3 text-sm text-gray-600">{user.phone || '-'}</td>
                            <td className="text-center p-3">
                                    <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${getRoleColor(user.role)}`}>
                                        {getRoleText(user.role)}
                                    </span>
                            </td>
                            <td className="text-center p-3">
                                {user.role !== 'ADMIN' ? (
                                    <select
                                        onChange={(e) => changeRole(user.id, e.target.value)}
                                        defaultValue={user.role}
                                        className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white"
                                    >
                                        <option value="CUSTOMER">Покупатель</option>
                                        <option value="MANAGER">Менеджер</option>
                                        <option value="ADMIN">Админ</option>
                                    </select>
                                ) : (
                                    <span className="text-sm text-gray-400">Нельзя изменить</span>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default AdminUsers;
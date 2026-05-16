import React, { useState } from 'react';
import api from '../../api/api';
import { useSearchAndFilters } from '../../hooks/useSearchAndFilters';
import { Search, X } from 'lucide-react';

function AdminUsers() {
    const {
        data: users,
        loading,
        totalItems,
        totalPages,
        currentPage,
        searchTerm,
        setSearchTerm,
        clearSearch,
        goToPage,
        reload: loadUsers
    } = useSearchAndFilters('/admin/users', {
        limit: 20,
        enableSearch: true,
        enableFilters: false
    });

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

    if (loading && users.length === 0) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Управление пользователями</h1>
                    <p className="text-sm text-gray-500 mt-1">Всего пользователей: {totalItems}</p>
                </div>
            </div>

            {/* Поиск */}
            <div className="mb-4">
                <div className="relative max-w-md">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                    <input
                        type="text"
                        placeholder="Поиск по логину, email или телефону..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-10 p-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                    />
                    {searchTerm && (
                        <button
                            onClick={clearSearch}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        >
                            <X size={16} />
                        </button>
                    )}
                </div>
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

            {users.length === 0 && !loading && (
                <div className="text-center py-10 text-gray-400">
                    {searchTerm ? 'По вашему запросу ничего не найдено' : 'Нет пользователей'}
                </div>
            )}

            {/* Пагинация */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-6 flex-wrap">
                    <button
                        onClick={() => goToPage(1)}
                        disabled={currentPage === 1}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === 1
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        « Первая
                    </button>
                    <button
                        onClick={() => goToPage(currentPage - 1)}
                        disabled={currentPage === 1}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === 1
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        ← Назад
                    </button>

                    <div className="flex gap-1">
                        {[...Array(Math.min(totalPages, 10))].map((_, i) => {
                            let page;
                            if (totalPages <= 7) {
                                page = i + 1;
                            } else if (currentPage <= 4) {
                                page = i + 1;
                            } else if (currentPage >= totalPages - 3) {
                                page = totalPages - 9 + i;
                            } else {
                                page = currentPage - 4 + i;
                            }

                            if (page >= 1 && page <= totalPages) {
                                return (
                                    <button
                                        key={page}
                                        onClick={() => goToPage(page)}
                                        className={`w-8 h-8 rounded-md text-sm transition ${
                                            currentPage === page
                                                ? 'bg-orange-500 text-white cursor-default'
                                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                                        }`}
                                    >
                                        {page}
                                    </button>
                                );
                            }
                            return null;
                        })}
                    </div>

                    <button
                        onClick={() => goToPage(currentPage + 1)}
                        disabled={currentPage === totalPages}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === totalPages
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        Вперед →
                    </button>
                    <button
                        onClick={() => goToPage(totalPages)}
                        disabled={currentPage === totalPages}
                        className={`px-3 py-1.5 rounded-md text-sm transition ${
                            currentPage === totalPages
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer'
                        }`}
                    >
                        Последняя »
                    </button>
                </div>
            )}
        </div>
    );
}

export default AdminUsers;
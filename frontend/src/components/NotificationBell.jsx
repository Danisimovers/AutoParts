import React, { useState, useEffect, useRef } from 'react';
import api from '../api/api';
import { Package, MessageSquare, Truck, Bell, CheckCircle } from 'lucide-react';

function NotificationBell() {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [showDropdown, setShowDropdown] = useState(false);
    const [loading, setLoading] = useState(false);
    const dropdownRef = useRef(null);

    useEffect(() => {
        loadNotifications();
        loadUnreadCount();

        const interval = setInterval(() => {
            loadUnreadCount();
        }, 30000);

        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('click', handleClickOutside);
        return () => document.removeEventListener('click', handleClickOutside);
    }, []);

    const loadNotifications = async () => {
        setLoading(true);
        try {
            const response = await api.get('/notifications');
            if (response.data.success) {
                setNotifications(response.data.data.notifications);
                setUnreadCount(response.data.data.unreadCount);
            }
        } catch (error) {
            console.error('Ошибка загрузки уведомлений:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadUnreadCount = async () => {
        try {
            const response = await api.get('/notifications/unread/count');
            if (response.data.success) {
                setUnreadCount(response.data.data.count);
            }
        } catch (error) {
            console.error('Ошибка загрузки счетчика:', error);
        }
    };

    const markAsRead = async (id) => {
        try {
            await api.put(`/notifications/${id}/read`);
            setNotifications(notifications.map(n =>
                n.id === id ? { ...n, read: true } : n
            ));
            setUnreadCount(Math.max(0, unreadCount - 1));
        } catch (error) {
            console.error('Ошибка отметки прочитанным:', error);
        }
    };

    const markAllAsRead = async () => {
        try {
            await api.put('/notifications/read-all');
            setNotifications(notifications.map(n => ({ ...n, read: true })));
            setUnreadCount(0);
        } catch (error) {
            console.error('Ошибка отметки всех:', error);
        }
    };

    const getNotificationIcon = (type) => {
        switch(type) {
            case 'VIN_REQUEST': return <Package size={16} className="text-blue-500" />;
            case 'VIN_RESPONSE': return <MessageSquare size={16} className="text-green-500" />;
            case 'ORDER_STATUS': return <Truck size={16} className="text-purple-500" />;
            default: return <Bell size={16} className="text-gray-500" />;
        }
    };

    return (
        <div ref={dropdownRef} className="relative">
            <button
                onClick={() => {
                    setShowDropdown(!showDropdown);
                    if (!showDropdown) loadNotifications();
                }}
                className="relative text-white text-2xl p-1 cursor-pointer bg-transparent border-none flex items-center"
            >
                <Bell size={20} />
                {unreadCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-red-500 text-white rounded-full text-[11px] w-[18px] h-[18px] flex items-center justify-center font-bold">
                        {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                )}
            </button>

            {showDropdown && (
                <div className="absolute top-9 right-0 w-80 max-h-[400px] bg-white rounded-lg shadow-lg z-[200] flex flex-col overflow-hidden">
                    <div className="px-4 py-3 border-b border-gray-200 bg-gray-50 flex justify-between items-center">
                        <strong className="text-sm">Уведомления</strong>
                        {notifications.length > 0 && (
                            <button
                                onClick={markAllAsRead}
                                className="bg-none border-none cursor-pointer text-xs text-orange-500 hover:text-orange-600"
                            >
                                Прочитать все
                            </button>
                        )}
                    </div>

                    <div className="overflow-y-auto max-h-[350px]">
                        {loading ? (
                            <div className="p-5 text-center text-gray-400">Загрузка...</div>
                        ) : notifications.length === 0 ? (
                            <div className="p-5 text-center text-gray-400">
                                Нет уведомлений
                            </div>
                        ) : (
                            notifications.map(notif => (
                                <div
                                    key={notif.id}
                                    onClick={() => {
                                        markAsRead(notif.id);
                                        if (notif.link) {
                                            window.location.href = notif.link;
                                        }
                                    }}
                                    className={`p-3 border-b border-gray-200 cursor-pointer transition-colors ${
                                        notif.read ? 'bg-white' : 'bg-blue-50'
                                    } hover:bg-gray-100`}
                                >
                                    <div className="flex gap-3">
                                        <span className="flex-shrink-0 mt-0.5">
                                            {getNotificationIcon(notif.type)}
                                        </span>
                                        <div className="flex-1">
                                            <div className="font-bold text-sm mb-1">
                                                {notif.title}
                                            </div>
                                            <div className="text-xs text-gray-500 mb-1">
                                                {notif.message}
                                            </div>
                                            <div className="text-[10px] text-gray-400">
                                                {new Date(notif.createdAt).toLocaleString()}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

export default NotificationBell;
import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

function UserVinRequestChat() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const [vinRequest, setVinRequest] = useState(null);
    const messagesEndRef = useRef(null);

    useEffect(() => {
        loadMessages();
        loadVinRequest();
        const interval = setInterval(loadMessages, 5000);
        return () => clearInterval(interval);
    }, [id]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const loadVinRequest = async () => {
        try {
            const response = await api.get(`/vin-requests/${id}`);
            if (response.data.success) {
                setVinRequest(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки заявки:', error);
        }
    };

    const loadMessages = async () => {
        try {
            const response = await api.get(`/vin-requests/${id}/messages`);
            if (response.data.success) {
                setMessages(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки сообщений:', error);
        } finally {
            setLoading(false);
        }
    };

    const sendMessage = async (e) => {
        e.preventDefault();
        if (!newMessage.trim()) return;

        setSending(true);
        try {
            const response = await api.post(`/vin-requests/${id}/messages`, { message: newMessage });
            if (response.data.success) {
                setNewMessage('');
                loadMessages();
            } else {
                alert(response.data.message);
            }
        } catch (error) {
            alert('Ошибка отправки сообщения');
        } finally {
            setSending(false);
        }
    };

    const getStatusColor = (status) => {
        switch(status) {
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800';
            case 'COMPLETED': return 'bg-green-100 text-green-800';
            case 'CANCELLED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'IN_PROGRESS': return 'В работе';
            case 'COMPLETED': return 'Завершён';
            case 'CANCELLED': return 'Отменён';
            default: return status;
        }
    };

    const isCurrentUser = (msg) => {
        return msg.senderId === user?.id;
    };

    if (loading) return (
        <div className="flex justify-center items-center h-64">
            <div className="text-gray-400">Загрузка...</div>
        </div>
    );

    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
            <button
                onClick={() => navigate('/profile')}
                className="mb-5 text-orange-600 hover:text-orange-700 transition"
            >
                ← Назад в профиль
            </button>

            <h1 className="text-2xl font-bold text-gray-800 mb-5">
                VIN-заявка #{id}
            </h1>

            {vinRequest && (
                <div className="bg-gray-50 p-4 rounded-lg mb-5 border border-gray-100">
                    <p className="text-sm text-gray-600 mb-1">
                        <span className="font-medium">VIN:</span> {vinRequest.vin}
                    </p>
                    <p className="text-sm text-gray-600 mb-1">
                        <span className="font-medium">Описание:</span> {vinRequest.description || '-'}
                    </p>
                    <p className="text-sm text-gray-600">
                        <span className="font-medium">Статус:</span>
                        <span className={`ml-2 px-2 py-0.5 rounded-full text-xs font-medium ${getStatusColor(vinRequest.status)}`}>
                            {getStatusText(vinRequest.status)}
                        </span>
                    </p>
                </div>
            )}

            <div className="border border-gray-200 rounded-lg h-[400px] overflow-y-auto p-4 bg-gray-50 mb-4">
                {messages.length === 0 ? (
                    <div className="text-center text-gray-400 py-10">
                        Нет сообщений. Ожидайте ответа менеджера.
                    </div>
                ) : (
                    messages.map(msg => {
                        const currentUser = isCurrentUser(msg);
                        return (
                            <div
                                key={msg.id}
                                className={`flex mb-3 ${currentUser ? 'justify-end' : 'justify-start'}`}
                            >
                                <div className={`max-w-[70%] ${currentUser ? 'bg-orange-500 text-white' : 'bg-white border border-gray-200 text-gray-700'} rounded-lg px-3 py-2`}>
                                    {!currentUser && (
                                        <div className="text-xs font-medium text-gray-500 mb-1">
                                            {msg.senderRole === 'MANAGER' ? 'Менеджер' :
                                                msg.senderRole === 'ADMIN' ? 'Админ' :
                                                    `Пользователь ${msg.senderLogin || ''}`}
                                        </div>
                                    )}
                                    {currentUser && (
                                        <div className="text-right text-xs font-medium text-orange-100 mb-1">
                                            Я
                                        </div>
                                    )}
                                    <div className="text-sm break-words">
                                        {msg.message}
                                    </div>
                                    <div className={`text-xs mt-1 opacity-70 ${currentUser ? 'text-right text-orange-100' : 'text-left text-gray-400'}`}>
                                        {new Date(msg.createdAt).toLocaleString()}
                                    </div>
                                </div>
                            </div>
                        );
                    })
                )}
                <div ref={messagesEndRef} />
            </div>

            <form onSubmit={sendMessage} className="flex gap-2">
                <textarea
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Напишите сообщение менеджеру..."
                    rows="3"
                    className="flex-1 p-2.5 border border-gray-200 rounded-lg resize-y focus:outline-none focus:ring-1 focus:ring-orange-500 font-sans text-sm"
                />
                <button
                    type="submit"
                    disabled={sending || !newMessage.trim()}
                    className="px-5 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition disabled:opacity-50 h-fit"
                >
                    {sending ? 'Отправка...' : 'Отправить'}
                </button>
            </form>
        </div>
    );
}

export default UserVinRequestChat;
import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/api';

function VinRequestChat() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const messagesEndRef = useRef(null);

    useEffect(() => {
        loadMessages();
        const interval = setInterval(loadMessages, 5000);
        return () => clearInterval(interval);
    }, [id]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const loadMessages = async () => {
        try {
            const response = await api.get(`/manager/vin-requests/${id}/messages`);
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
            const response = await api.post(`/manager/vin-requests/${id}/messages`, { message: newMessage });
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

    const isCurrentUser = (msg) => {
        return msg.senderId === user?.id;
    };

    const getSenderName = (msg) => {
        if (msg.senderRole === 'CUSTOMER') {
            return `Пользователь ${msg.senderLogin || ''}`;
        }
        if (msg.senderRole === 'MANAGER') return 'Менеджер';
        if (msg.senderRole === 'ADMIN') return 'Админ';
        return msg.senderLogin || 'Неизвестный';
    };

    if (loading) return (
        <div className="p-8 text-center text-gray-400">
            Загрузка...
        </div>
    );

    return (
        <div className="p-6 flex-1 max-w-4xl mx-auto">
            <button
                onClick={() => navigate('/manager/vin-requests')}
                className="mb-5 text-orange-600 hover:text-orange-700 transition"
            >
                ← Назад к заявкам
            </button>

            <h1 className="text-2xl font-bold text-gray-800 mb-5">
                Переписка по заявке #{id}
            </h1>

            <div className="border border-gray-200 rounded-xl h-[400px] overflow-y-auto p-4 bg-gray-50 mb-4">
                {messages.length === 0 ? (
                    <div className="text-center text-gray-400 py-10">
                        Нет сообщений. Напишите ответ пользователю.
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
                                            {getSenderName(msg)}
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
                    placeholder="Введите ваш ответ..."
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

export default VinRequestChat;
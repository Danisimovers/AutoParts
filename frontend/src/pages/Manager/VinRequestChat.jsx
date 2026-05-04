import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/api';

function VinRequestChat() {
    const { id } = useParams();
    const navigate = useNavigate();
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

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={() => navigate('/manager/vin-requests')} style={{ marginBottom: '20px' }}>
                ← Назад к заявкам
            </button>

            <h1>Переписка по заявке #{id}</h1>

            <div style={{
                border: '1px solid #ddd',
                borderRadius: '8px',
                height: '400px',
                overflowY: 'auto',
                padding: '15px',
                marginBottom: '20px',
                backgroundColor: '#f9f9f9'
            }}>
                {messages.length === 0 ? (
                    <div style={{ textAlign: 'center', color: '#999', padding: '40px' }}>
                        Нет сообщений. Напишите ответ пользователю.
                    </div>
                ) : (
                    messages.map(msg => {
                        const isManager = msg.senderId !== null; // Упрощенно, нужно проверять по роли
                        return (
                            <div
                                key={msg.id}
                                style={{
                                    textAlign: isManager ? 'right' : 'left',
                                    marginBottom: '15px'
                                }}
                            >
                                <div style={{
                                    display: 'inline-block',
                                    maxWidth: '70%',
                                    padding: '10px 15px',
                                    borderRadius: '12px',
                                    backgroundColor: isManager ? '#e67e22' : '#e0e0e0',
                                    color: isManager ? 'white' : '#333'
                                }}>
                                    <div style={{ fontSize: '14px' }}>{msg.message}</div>
                                    <div style={{ fontSize: '10px', marginTop: '5px', opacity: 0.7 }}>
                                        {new Date(msg.createdAt).toLocaleString()}
                                    </div>
                                </div>
                            </div>
                        );
                    })
                )}
                <div ref={messagesEndRef} />
            </div>

            <form onSubmit={sendMessage} style={{ display: 'flex', gap: '10px' }}>
                <textarea
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Введите ваш ответ..."
                    rows="3"
                    style={{
                        flex: 1,
                        padding: '10px',
                        border: '1px solid #ddd',
                        borderRadius: '8px',
                        resize: 'vertical'
                    }}
                />
                <button
                    type="submit"
                    disabled={sending || !newMessage.trim()}
                    style={{
                        padding: '10px 20px',
                        backgroundColor: '#e67e22',
                        color: 'white',
                        border: 'none',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        height: 'fit-content'
                    }}
                >
                    {sending ? 'Отправка...' : 'Отправить'}
                </button>
            </form>
        </div>
    );
}

export default VinRequestChat;
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/api';

function VinRequests() {
    const navigate = useNavigate();
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadRequests();
    }, []);

    const loadRequests = async () => {
        setLoading(true);
        try {
            const response = await api.get('/manager/vin-requests');
            if (response.data.success) {
                setRequests(response.data.data);
            }
        } catch (error) {
            console.error('Ошибка загрузки заявок:', error);
            alert('Ошибка загрузки заявок');
        } finally {
            setLoading(false);
        }
    };

    const updateStatus = async (id, status) => {
        try {
            await api.put(`/manager/vin-requests/${id}/status?status=${status}`);
            loadRequests();
            alert('Статус обновлен');
        } catch (error) {
            alert('Ошибка обновления статуса');
        }
    };

    const getStatusColor = (status) => {
        switch(status) {
            case 'PENDING': return '#ff9800';
            case 'PROCESSING': return '#2196f3';
            case 'COMPLETED': return '#4caf50';
            case 'REJECTED': return '#f44336';
            default: return '#666';
        }
    };

    const getStatusText = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'PROCESSING': return 'В обработке';
            case 'COMPLETED': return 'Выполнена';
            case 'REJECTED': return 'Отклонена';
            default: return status;
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    return (
        <div>
            <h1>Заявки по VIN</h1>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>ID</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>ID пользователя</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>VIN</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Описание</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Статус</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Действия</th>
                </tr>
                </thead>
                <tbody>
                {requests.map(req => (
                    <tr key={req.id} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '10px' }}>{req.id}</td>
                        <td style={{ padding: '10px' }}>{req.userId}</td>
                        <td style={{ padding: '10px' }}><code>{req.vin}</code></td>
                        <td style={{ padding: '10px' }}>{req.description || '-'}</td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <span style={{
                                backgroundColor: getStatusColor(req.status),
                                color: 'white',
                                padding: '4px 12px',
                                borderRadius: '20px',
                                fontSize: '12px'
                            }}>
                                {getStatusText(req.status)}
                            </span>
                        </td>
                        <td style={{ textAlign: 'center', padding: '10px' }}>
                            <select
                                onChange={(e) => updateStatus(req.id, e.target.value)}
                                defaultValue={req.status}
                                style={{ padding: '5px 10px', borderRadius: '4px', border: '1px solid #ddd', marginRight: '10px' }}
                            >
                                <option value="PENDING">Ожидает</option>
                                <option value="PROCESSING">В обработке</option>
                                <option value="COMPLETED">Выполнена</option>
                                <option value="REJECTED">Отклонена</option>
                            </select>
                            <button
                                onClick={() => navigate(`/manager/vin-requests/${req.id}`)}
                                style={{
                                    padding: '5px 10px',
                                    backgroundColor: '#e67e22',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '4px',
                                    cursor: 'pointer'
                                }}
                            >
                                Ответить
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default VinRequests;
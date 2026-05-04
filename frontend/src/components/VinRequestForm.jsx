import React, { useState } from 'react';
import api from '../api/api';

function VinRequestForm({ onClose, onSuccess }) {
    const [vin, setVin] = useState('');
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            const response = await api.post('/vin-requests', { vin, description });
            if (response.data.success) {
                setSuccess(response.data.message);
                setVin('');
                setDescription('');
                setTimeout(() => {
                    if (onClose) onClose();
                    if (onSuccess) onSuccess();
                }, 2000);
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Ошибка отправки заявки');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000
        }}>
            <div style={{
                backgroundColor: '#fff',
                padding: '30px',
                borderRadius: '12px',
                maxWidth: '500px',
                width: '90%',
                boxShadow: '0 10px 30px rgba(0,0,0,0.2)'
            }}>
                <h2 style={{ marginBottom: '20px', color: '#333', fontSize: '24px' }}>Запрос на подбор запчастей</h2>

                {error && (
                    <div style={{
                        backgroundColor: '#ffebee',
                        color: '#c62828',
                        padding: '12px',
                        borderRadius: '8px',
                        marginBottom: '15px',
                        fontSize: '14px'
                    }}>
                        {error}
                    </div>
                )}

                {success && (
                    <div style={{
                        backgroundColor: '#e8f5e9',
                        color: '#2e7d32',
                        padding: '12px',
                        borderRadius: '8px',
                        marginBottom: '15px',
                        fontSize: '14px'
                    }}>
                        {success}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div style={{ marginBottom: '15px' }}>
                        <label style={{ display: 'block', marginBottom: '8px', color: '#555', fontWeight: '500' }}>
                            VIN номер автомобиля *
                        </label>
                        <input
                            type="text"
                            value={vin}
                            onChange={(e) => setVin(e.target.value.toUpperCase())}
                            placeholder="XTA12345678901234"
                            required
                            style={{
                                width: '100%',
                                padding: '12px',
                                border: '1px solid #ddd',
                                borderRadius: '8px',
                                fontSize: '14px',
                                fontFamily: 'monospace'
                            }}
                        />
                        <small style={{ color: '#999', fontSize: '12px', marginTop: '5px', display: 'block' }}>
                            Пример: XTA12345678901234
                        </small>
                    </div>

                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '8px', color: '#555', fontWeight: '500' }}>
                            Описание (что нужно)
                        </label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Опишите, какие запчасти нужны (необязательно)"
                            rows="4"
                            style={{
                                width: '100%',
                                padding: '12px',
                                border: '1px solid #ddd',
                                borderRadius: '8px',
                                fontSize: '14px',
                                fontFamily: 'Arial, sans-serif'
                            }}
                        />
                    </div>

                    <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                        <button
                            type="button"
                            onClick={onClose}
                            style={{
                                padding: '10px 20px',
                                backgroundColor: '#f5f5f5',
                                color: '#666',
                                border: '1px solid #ddd',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                transition: '0.3s'
                            }}
                            onMouseEnter={(e) => {
                                e.target.style.backgroundColor = '#eee';
                            }}
                            onMouseLeave={(e) => {
                                e.target.style.backgroundColor = '#f5f5f5';
                            }}
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            style={{
                                padding: '10px 24px',
                                backgroundColor: '#e67e22',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '500',
                                transition: '0.3s'
                            }}
                            onMouseEnter={(e) => {
                                e.target.style.backgroundColor = '#d35400';
                            }}
                            onMouseLeave={(e) => {
                                e.target.style.backgroundColor = '#e67e22';
                            }}
                        >
                            {loading ? 'Отправка...' : 'Отправить'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default VinRequestForm;
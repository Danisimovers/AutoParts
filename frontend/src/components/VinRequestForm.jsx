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
                backgroundColor: 'white',
                padding: '30px',
                borderRadius: '8px',
                maxWidth: '500px',
                width: '90%'
            }}>
                <h2 style={{ marginBottom: '20px' }}>Запрос на подбор запчастей</h2>

                {error && (
                    <div style={{ backgroundColor: '#ffebee', color: '#c62828', padding: '10px', borderRadius: '4px', marginBottom: '15px' }}>
                        {error}
                    </div>
                )}

                {success && (
                    <div style={{ backgroundColor: '#d4edda', color: '#155724', padding: '10px', borderRadius: '4px', marginBottom: '15px' }}>
                        {success}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div style={{ marginBottom: '15px' }}>
                        <label style={{ display: 'block', marginBottom: '5px' }}>VIN номер автомобиля *</label>
                        <input
                            type="text"
                            value={vin}
                            onChange={(e) => setVin(e.target.value.toUpperCase())}
                            placeholder="Введите 17-значный VIN код"
                            required
                            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                        />
                        <small style={{ color: '#666' }}>Пример: XTA12345678901234</small>
                    </div>

                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '5px' }}>Описание (что нужно)</label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Опишите, какие запчасти нужны (необязательно)"
                            rows="4"
                            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                        />
                    </div>

                    <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                        <button
                            type="button"
                            onClick={onClose}
                            style={{ padding: '10px 20px', backgroundColor: '#666', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            style={{ padding: '10px 20px', backgroundColor: '#e67e22', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
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
import React, { useState, useEffect } from 'react';
import api from '../api/api';

function VinRequestForm({ onClose, onSuccess }) {
    const [vin, setVin] = useState('');
    const [description, setDescription] = useState('');
    const [userVehicles, setUserVehicles] = useState([]);
    const [selectedVehicleId, setSelectedVehicleId] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Загружаем автомобили пользователя
    useEffect(() => {
        loadUserVehicles();
    }, []);

    const loadUserVehicles = async () => {
        try {
            const response = await api.get('/user-vehicles');
            if (response.data.success) {
                setUserVehicles(response.data.data || []);
            }
        } catch (error) {
            console.error('Ошибка загрузки автомобилей:', error);
        }
    };

    // Валидация VIN номера
    const validateVin = (vin) => {
        const vinRegex = /^[A-HJ-NPR-Z0-9]{17}$/;
        return vinRegex.test(vin);
    };

    // При выборе автомобиля из списка, подставляем его VIN
    const handleVehicleSelect = (vehicleId) => {
        setSelectedVehicleId(vehicleId);
        const selectedVehicle = userVehicles.find(v => v.id === parseInt(vehicleId));
        if (selectedVehicle && selectedVehicle.vin) {
            setVin(selectedVehicle.vin);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        // Проверка VIN
        if (!vin.trim()) {
            setError('VIN номер обязателен');
            return;
        }
        if (!validateVin(vin)) {
            setError('VIN номер должен содержать 17 символов (латиница и цифры, без букв I, O, Q)');
            return;
        }

        // Проверка описания
        if (!description.trim()) {
            setError('Опишите, какие запчасти нужны');
            return;
        }

        setLoading(true);

        try {
            const response = await api.post('/vin-requests', {
                vin,
                description,
                userVehicleId: selectedVehicleId || null
            });
            if (response.data.success) {
                setSuccess(response.data.message);
                setVin('');
                setDescription('');
                setSelectedVehicleId('');
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
        <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-[1000]">
            <div className="bg-white p-[30px] rounded-xl max-w-[500px] w-[90%] shadow-xl">
                <h2 className="mb-5 text-[#333] text-2xl">
                    Запрос на подбор запчастей
                </h2>

                {error && (
                    <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">
                        {error}
                    </div>
                )}

                {success && (
                    <div className="bg-green-50 text-green-700 p-3 rounded-lg mb-4 text-sm">
                        {success}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    {/* Выбор автомобиля из сохраненных */}
                    {userVehicles.length > 0 && (
                        <div className="mb-4">
                            <label className="block mb-2 text-[#555] font-medium">
                                Выбрать из моих автомобилей
                            </label>
                            <select
                                value={selectedVehicleId}
                                onChange={(e) => handleVehicleSelect(e.target.value)}
                                className="w-full p-3 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                            >
                                <option value="">-- Выберите автомобиль --</option>
                                {userVehicles.map(vehicle => (
                                    <option key={vehicle.id} value={vehicle.id}>
                                        {vehicle.nickname || `${vehicle.make} ${vehicle.model || ''}`}
                                        {vehicle.vin ? ` (${vehicle.vin.slice(-6)})` : ''}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    <div className="mb-4">
                        <label className="block mb-2 text-[#555] font-medium">
                            VIN номер автомобиля *
                        </label>
                        <input
                            type="text"
                            value={vin}
                            onChange={(e) => setVin(e.target.value.toUpperCase())}
                            placeholder="XTA12345678901234"
                            required
                            className="w-full p-3 border border-gray-300 rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                        />
                        <small className="text-gray-400 text-xs mt-1 block">
                            VIN код состоит из 17 символов (латиница и цифры, без букв I, O, Q)
                        </small>
                    </div>

                    <div className="mb-5">
                        <label className="block mb-2 text-[#555] font-medium">
                            Описание (что нужно) *
                        </label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Опишите, какие запчасти нужны"
                            rows="4"
                            required
                            className="w-full p-3 border border-gray-300 rounded-lg text-sm font-sans focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent resize-y"
                        />
                    </div>

                    <div className="flex gap-3 justify-end">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-5 py-2.5 bg-gray-100 text-gray-600 border border-gray-300 rounded-lg cursor-pointer text-sm transition-colors duration-300 hover:bg-gray-200"
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className={`
                                px-6 py-2.5 bg-orange-500 text-white border-none rounded-lg 
                                text-sm font-medium transition-colors duration-300
                                ${loading ? 'opacity-50 cursor-not-allowed' : 'hover:bg-orange-600 cursor-pointer'}
                            `}
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
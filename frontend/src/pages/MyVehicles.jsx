// src/pages/MyVehicles.js
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';
import { Car, Plus, Trash2, X, AlertCircle } from 'lucide-react';

function MyVehicles() {
    const { user } = useAuth();
    const [vehicles, setVehicles] = useState([]);
    const [allVehicles, setAllVehicles] = useState([]); // все доступные модели из таблицы vehicles
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        vehicleId: '',
        vin: '',
        licensePlate: '',
        nickname: '',
        year: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (user) {
            loadUserVehicles();
            loadAllVehicles();
        }
    }, [user]);

    const loadUserVehicles = async () => {
        try {
            const response = await api.get('/user-vehicles');
            if (response.data.success) {
                setVehicles(response.data.data || []);
            }
        } catch (error) {
            console.error('Ошибка загрузки автомобилей:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadAllVehicles = async () => {
        try {
            const response = await api.get('/vehicles');
            if (response.data.success) {
                setAllVehicles(response.data.data || []);
            }
        } catch (error) {
            console.error('Ошибка загрузки моделей:', error);
        }
    };

    const handleAddVehicle = async (e) => {
        e.preventDefault();
        if (!formData.vehicleId) {
            alert('Выберите марку и модель автомобиля');
            return;
        }

        setSubmitting(true);
        try {
            await api.post('/user-vehicles', formData);
            alert('Автомобиль успешно добавлен');
            setShowModal(false);
            setFormData({
                vehicleId: '',
                vin: '',
                licensePlate: '',
                nickname: '',
                year: ''
            });
            loadUserVehicles();
        } catch (error) {
            alert(error.response?.data?.message || 'Ошибка добавления автомобиля');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteVehicle = async (id) => {
        if (!window.confirm('Вы уверены, что хотите удалить этот автомобиль?')) return;

        try {
            await api.delete(`/user-vehicles/${id}`);
            alert('Автомобиль удален');
            loadUserVehicles();
        } catch (error) {
            alert('Ошибка удаления');
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="text-gray-400">Загрузка...</div>
            </div>
        );
    }

    return (
        <div className="w-full px-4 py-8">
            <div className="max-w-6xl mx-auto">
                <div className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold text-gray-800 flex items-center gap-3">
                        <Car size={28} className="text-orange-500" />
                        Мои автомобили
                    </h1>
                    <button
                        onClick={() => setShowModal(true)}
                        className="flex items-center gap-2 px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition"
                    >
                        <Plus size={18} />
                        Добавить авто
                    </button>
                </div>

                {vehicles.length === 0 ? (
                    <div className="text-center py-12 bg-white rounded-xl border border-gray-200">
                        <Car size={48} className="text-gray-300 mx-auto mb-4" />
                        <p className="text-gray-400">У вас пока нет добавленных автомобилей</p>
                        <button
                            onClick={() => setShowModal(true)}
                            className="mt-4 bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition"
                        >
                            Добавить первый автомобиль
                        </button>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {vehicles.map(vehicle => (
                            <div key={vehicle.id} className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-md transition">
                                <div className="flex justify-between items-start mb-3">
                                    <div>
                                        <h3 className="text-lg font-bold text-gray-800">
                                            {vehicle.nickname || `${vehicle.make} ${vehicle.model || ''}`}
                                        </h3>
                                        <p className="text-sm text-gray-500 mt-1">
                                            {vehicle.make} {vehicle.model} {vehicle.generation}
                                        </p>
                                    </div>
                                    <button
                                        onClick={() => handleDeleteVehicle(vehicle.id)}
                                        className="text-red-400 hover:text-red-600 transition"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>

                                <div className="space-y-2 text-sm">
                                    {vehicle.vin && (
                                        <p className="text-gray-600">
                                            <span className="font-medium">VIN:</span> {vehicle.vin}
                                        </p>
                                    )}
                                    {vehicle.licensePlate && (
                                        <p className="text-gray-600">
                                            <span className="font-medium">Госномер:</span> {vehicle.licensePlate}
                                        </p>
                                    )}
                                    {vehicle.year && (
                                        <p className="text-gray-600">
                                            <span className="font-medium">Год выпуска:</span> {vehicle.year}
                                        </p>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Модальное окно добавления автомобиля */}
            {showModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl">
                        <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                            <h2 className="text-xl font-bold text-gray-800">Добавление автомобиля</h2>
                            <button
                                onClick={() => setShowModal(false)}
                                className="text-gray-400 hover:text-gray-600 transition"
                            >
                                <X size={24} />
                            </button>
                        </div>

                        <form onSubmit={handleAddVehicle} className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Марка и модель <span className="text-red-500">*</span>
                                </label>
                                <select
                                    value={formData.vehicleId}
                                    onChange={(e) => setFormData({...formData, vehicleId: e.target.value})}
                                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                                    required
                                >
                                    <option value="">Выберите автомобиль</option>
                                    {allVehicles.map(vehicle => (
                                        <option key={vehicle.id} value={vehicle.id}>
                                            {vehicle.make} {vehicle.model || ''} {vehicle.generation || ''}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    VIN номер
                                </label>
                                <input
                                    type="text"
                                    value={formData.vin}
                                    onChange={(e) => setFormData({...formData, vin: e.target.value.toUpperCase()})}
                                    placeholder="XTA12345678901234"
                                    maxLength="17"
                                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Государственный номер
                                </label>
                                <input
                                    type="text"
                                    value={formData.licensePlate}
                                    onChange={(e) => setFormData({...formData, licensePlate: e.target.value.toUpperCase()})}
                                    placeholder="А123ВС77"
                                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Год выпуска
                                </label>
                                <input
                                    type="number"
                                    value={formData.year}
                                    onChange={(e) => setFormData({...formData, year: e.target.value})}
                                    placeholder="2020"
                                    min="1950"
                                    max={new Date().getFullYear()}
                                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Псевдоним (необязательно)
                                </label>
                                <input
                                    type="text"
                                    value={formData.nickname}
                                    onChange={(e) => setFormData({...formData, nickname: e.target.value})}
                                    placeholder="Моя ласточка"
                                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                                />
                            </div>

                            <div className="flex gap-3 pt-4">
                                <button
                                    type="button"
                                    onClick={() => setShowModal(false)}
                                    className="flex-1 px-4 py-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition"
                                >
                                    Отмена
                                </button>
                                <button
                                    type="submit"
                                    disabled={submitting}
                                    className="flex-1 px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition disabled:opacity-50"
                                >
                                    {submitting ? 'Добавление...' : 'Добавить'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

export default MyVehicles;
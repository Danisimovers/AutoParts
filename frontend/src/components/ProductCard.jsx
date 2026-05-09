import React from 'react';
import { useNavigate } from 'react-router-dom';

function ProductCard({ product, onAddToCart }) {
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/product/${product.id}`);
    };

    return (
        <div
            onClick={handleCardClick}
            className={`
                border border-gray-300 rounded-lg p-4 m-2 w-[250px] 
                flex flex-col bg-white cursor-pointer 
                transition-shadow duration-200 h-full min-h-[280px]
                hover:shadow-md
            `}
        >
            <h3 className="text-lg mb-2 min-h-[48px] m-0">
                {product.name}
            </h3>

            <p className="text-gray-500 text-sm my-1">
                Артикул: {product.sku}
            </p>

            <p className="text-xl font-bold text-[#e67e22] my-2">
                {product.price.toLocaleString()} ₽
            </p>

            <p className={`
                text-xs my-1
                ${product.stock > 0 ? 'text-green-600' : 'text-red-600'}
            `}>
                {product.stock > 0 ? `В наличии: ${product.stock} шт` : 'Нет в наличии'}
            </p>

            <div className="flex-1" />

            <button
                onClick={(e) => {
                    e.stopPropagation();
                    onAddToCart(product);
                }}
                disabled={product.stock === 0}
                className={`
                    w-full mt-3 py-2.5 px-4 rounded-md text-sm font-medium
                    text-white border-none transition-colors duration-200
                    ${product.stock > 0
                    ? 'bg-[#e67e22] hover:bg-[#d35400] cursor-pointer'
                    : 'bg-[#e67e22] cursor-not-allowed opacity-50'
                }
                `}
            >
                {product.stock > 0 ? 'В корзину' : 'Нет в наличии'}
            </button>
        </div>
    );
}

export default ProductCard;
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
            style={{
                border: '1px solid #ddd',
                borderRadius: '8px',
                padding: '16px',
                margin: '8px',
                width: '250px',
                display: 'flex',
                flexDirection: 'column',
                verticalAlign: 'top',
                cursor: 'pointer',
                backgroundColor: '#fff',
                transition: 'box-shadow 0.2s',
                height: '100%',
                minHeight: '280px'
            }}
            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)'}
            onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
        >
            <h3 style={{
                fontSize: '18px',
                margin: '0 0 8px 0',
                minHeight: '48px'
            }}>
                {product.name}
            </h3>

            <p style={{
                color: '#666',
                fontSize: '14px',
                margin: '4px 0'
            }}>
                Артикул: {product.sku}
            </p>

            <p style={{
                fontSize: '20px',
                fontWeight: 'bold',
                color: '#e67e22',
                margin: '8px 0'
            }}>
                {product.price.toLocaleString()} ₽
            </p>

            <p style={{
                fontSize: '12px',
                color: product.stock > 0 ? 'green' : 'red',
                margin: '4px 0'
            }}>
                {product.stock > 0 ? `В наличии: ${product.stock} шт` : 'Нет в наличии'}
            </p>

            <div style={{ flex: 1 }} /> {/* Это растягивает пространство и прижимает кнопку вниз */}

            <button
                onClick={(e) => {
                    e.stopPropagation();
                    onAddToCart(product);
                }}
                disabled={product.stock === 0}
                style={{
                    backgroundColor: '#e67e22',
                    color: 'white',
                    border: 'none',
                    padding: '10px 16px',
                    borderRadius: '6px',
                    cursor: product.stock > 0 ? 'pointer' : 'not-allowed',
                    width: '100%',
                    marginTop: '12px',
                    fontSize: '14px',
                    fontWeight: '500',
                    transition: 'background-color 0.2s',
                    opacity: product.stock === 0 ? 0.5 : 1
                }}
                onMouseEnter={(e) => {
                    if (product.stock > 0) {
                        e.currentTarget.style.backgroundColor = '#d35400';
                    }
                }}
                onMouseLeave={(e) => {
                    if (product.stock > 0) {
                        e.currentTarget.style.backgroundColor = '#e67e22';
                    }
                }}
            >
                {product.stock > 0 ? 'В корзину' : 'Нет в наличии'}
            </button>
        </div>
    );
}

export default ProductCard;
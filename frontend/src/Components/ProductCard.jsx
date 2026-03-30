import React from 'react';

function ProductCard({ product, onAddToCart }) {
    return (
        <div style={{
            border: '1px solid #ddd',
            borderRadius: '8px',
            padding: '16px',
            margin: '8px',
            width: '250px',
            display: 'inline-block',
            verticalAlign: 'top'
        }}>
            <h3 style={{ fontSize: '18px', margin: '0 0 8px 0' }}>{product.name}</h3>
            <p style={{ color: '#666', fontSize: '14px' }}>Артикул: {product.sku}</p>
            <p style={{ fontSize: '20px', fontWeight: 'bold', color: '#e67e22' }}>
                {product.price} ₽
            </p>
            <p style={{ fontSize: '12px', color: product.stock > 0 ? 'green' : 'red' }}>
                {product.stock > 0 ? `В наличии: ${product.stock} шт` : 'Нет в наличии'}
            </p>
            <button
                onClick={() => onAddToCart(product)}
                disabled={product.stock === 0}
                style={{
                    backgroundColor: '#e67e22',
                    color: 'white',
                    border: 'none',
                    padding: '8px 16px',
                    borderRadius: '4px',
                    cursor: product.stock > 0 ? 'pointer' : 'not-allowed',
                    width: '100%'
                }}
            >
                {product.stock > 0 ? 'В корзину' : 'Нет в наличии'}
            </button>
        </div>
    );
}

export default ProductCard;
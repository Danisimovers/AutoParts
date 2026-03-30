import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Catalog from './pages/Catalog';
import Cart from './pages/Cart';
import OrderSuccess from './pages/OrderSuccess';

function App() {
  return (
      <BrowserRouter>
        <div>
          <nav style={{
            backgroundColor: '#333',
            padding: '15px',
            display: 'flex',
            gap: '20px'
          }}>
            <Link to="/" style={{ color: 'white', textDecoration: 'none' }}>Каталог</Link>
            <Link to="/cart" style={{ color: 'white', textDecoration: 'none' }}>Корзина</Link>
          </nav>

          <Routes>
            <Route path="/" element={<Catalog />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/order-success" element={<OrderSuccess />} />
          </Routes>
        </div>
      </BrowserRouter>
  );
}

export default App;
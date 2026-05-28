import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import VerifyOtp from './pages/VerifyOtp';
import { getCurrentUser } from './api/auth';

const PrivateRoute = ({ children }) => {
  const user = getCurrentUser();
  return user ? children : <Navigate to="/login" />;
};

const Home = () => {
  const user = getCurrentUser();
  return (
    <div className="auth-container" style={{ textAlign: 'center' }}>
      <h1>Mediflow Dashboard</h1>
      <p className="subtitle">Welcome back, {user?.username}!</p>
      <div className="alert alert-success">
        You are successfully logged in as {user?.roles?.join(', ')}.
      </div>
      <button 
        className="btn" 
        onClick={() => { localStorage.removeItem('user'); window.location.href = '/login'; }}
        style={{ marginTop: '1rem', background: 'rgba(239, 68, 68, 0.2)', border: '1px solid rgba(239, 68, 68, 0.4)', color: '#ef4444' }}
      >
        Sign Out
      </button>
    </div>
  );
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-otp" element={<VerifyOtp />} />
        <Route 
          path="/" 
          element={
            <PrivateRoute>
              <Home />
            </PrivateRoute>
          } 
        />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;

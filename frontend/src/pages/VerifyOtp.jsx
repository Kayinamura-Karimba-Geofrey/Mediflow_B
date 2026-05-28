import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { CheckCircle, ShieldCheck, AlertCircle } from 'lucide-react';
import { verifyOtp } from '../api/auth';

const VerifyOtp = () => {
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const username = location.state?.username;

  useEffect(() => {
    if (!username) {
      navigate('/register');
    }
  }, [username, navigate]);

  const handleChange = (element, index) => {
    if (isNaN(element.value)) return false;

    setOtp([...otp.map((d, idx) => (idx === index ? element.value : d))]);

    // Focus next input
    if (element.nextSibling && element.value !== '') {
      element.nextSibling.focus();
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    const otpString = otp.join('');
    if (otpString.length < 6) {
      setError('Please enter all 6 digits');
      setLoading(false);
      return;
    }

    try {
      await verifyOtp({ username, otp: otpString });
      setSuccess('Account verified successfully! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h1>Verify Account</h1>
      <p className="subtitle">Enter the 6-digit code sent to your email</p>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <CheckCircle size={18} />
          {success}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="otp-inputs">
          {otp.map((data, index) => {
            return (
              <input
                className="otp-input"
                type="text"
                name="otp"
                maxLength="1"
                key={index}
                value={data}
                onChange={e => handleChange(e.target, index)}
                onFocus={e => e.target.select()}
              />
            );
          })}
        </div>

        <button type="submit" className="btn" disabled={loading}>
          {loading ? <div className="spinner" /> : <><ShieldCheck size={18} /> Verify OTP</>}
        </button>
      </form>

      <p className="link-text">
        Didn't receive a code? 
        <a href="#" onClick={(e) => { e.preventDefault(); /* Implement resend logic if backend supports it */ }}>Resend Code</a>
      </p>
    </div>
  );
};

export default VerifyOtp;

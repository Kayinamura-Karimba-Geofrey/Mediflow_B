import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth';

const authApi = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const signup = async (userData) => {
  const response = await authApi.post('/signup', userData);
  return response.data;
};

export const signin = async (credentials) => {
  const response = await authApi.post('/signin', credentials);
  if (response.data.accessToken) {
    localStorage.setItem('user', JSON.stringify(response.data));
  }
  return response.data;
};

export const verifyOtp = async (otpData) => {
  const response = await authApi.post('/verify-otp', otpData);
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('user');
};

export const getCurrentUser = () => {
  return JSON.parse(localStorage.getItem('user'));
};

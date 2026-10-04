import axios from 'axios';

// Local dev: '/api' (Vite proxies it to the Spring Boot backend).
// Production: set VITE_API_URL, e.g. https://bank-api.onrender.com/api
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api' });

// Attach the JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the token expired or is missing, send the user back to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const onLoginPage = window.location.pathname === '/login';
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (!onLoginPage) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Extract the backend's error message: { message, status }
export function errorMessage(error) {
  return error.response?.data?.message || error.message || 'Something went wrong';
}

export function formatINR(amount) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount ?? 0);
}

export function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function formatDateTime(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default api;

import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 15000
});


// Attach token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('findseat_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Global error handler
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('findseat_token');
      localStorage.removeItem('findseat_user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;

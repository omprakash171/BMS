import { createContext, useContext, useState } from 'react';
import api from '../services/api.js';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('user'));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);
  const [token, setToken] = useState(localStorage.getItem('token'));

  function saveSession(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem(
      'user',
      JSON.stringify({ username: data.username, role: data.role, customerId: data.customerId })
    );
    setToken(data.token);
    setUser({ username: data.username, role: data.role, customerId: data.customerId });
  }

  async function login(username, password) {
    const { data } = await api.post('/auth/login', { username, password });
    saveSession(data);
    return data;
  }

  async function register(form) {
    const { data } = await api.post('/auth/register', form);
    saveSession(data);
    return data;
  }

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

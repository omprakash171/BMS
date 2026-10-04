import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { errorMessage } from '../services/api.js';

const EMPTY = {
  username: '',
  password: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  address: '',
  dateOfBirth: '',
};

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register({ ...form, dateOfBirth: form.dateOfBirth || null });
      navigate('/customer/dashboard', { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: 520 }}>
        <h1>🏦 Open an Account</h1>
        <p className="subtitle">Register as a new customer - a savings account is created automatically</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>First Name</label>
              <input className="form-control" value={form.firstName} onChange={update('firstName')} required />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input className="form-control" value={form.lastName} onChange={update('lastName')} required />
            </div>
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" className="form-control" value={form.email} onChange={update('email')} required />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Phone</label>
              <input className="form-control" value={form.phone} onChange={update('phone')} required />
            </div>
            <div className="form-group">
              <label>Date of Birth</label>
              <input type="date" className="form-control" value={form.dateOfBirth} onChange={update('dateOfBirth')} />
            </div>
          </div>
          <div className="form-group">
            <label>Address</label>
            <input className="form-control" value={form.address} onChange={update('address')} />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Username</label>
              <input className="form-control" value={form.username} onChange={update('username')} required minLength={3} />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" className="form-control" value={form.password} onChange={update('password')} required minLength={6} />
            </div>
          </div>
          <button className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating account…' : 'Register'}
          </button>
        </form>

        <p style={{ marginTop: 16, fontSize: 14 }}>
          Already have an account? <Link to="/login" className="link">Login</Link>
        </p>
      </div>
    </div>
  );
}

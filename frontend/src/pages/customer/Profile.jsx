import { useEffect, useState } from 'react';
import api, { errorMessage, formatDate } from '../../services/api.js';
import StatusBadge from '../../components/StatusBadge.jsx';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/customers/profile')
      .then((res) => {
        setProfile(res.data);
        setForm({
          firstName: res.data.firstName,
          lastName: res.data.lastName,
          email: res.data.email,
          phone: res.data.phone,
          address: res.data.address || '',
          dateOfBirth: res.data.dateOfBirth || '',
        });
      })
      .catch((err) => setError(errorMessage(err)));
  }, []);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const { data } = await api.put('/customers/profile', {
        ...form,
        dateOfBirth: form.dateOfBirth || null,
      });
      setProfile(data);
      setSuccess('Profile updated successfully');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  if (error && !profile) return <div className="alert alert-error">{error}</div>;
  if (!form) return <div className="loading">Loading profile…</div>;

  return (
    <div style={{ maxWidth: 640 }}>
      <div className="page-header">
        <h2>My Profile</h2>
        <p>Customer ID: <span className="mono">{profile.customerId}</span> · Status: <StatusBadge value={profile.status} /> · Joined: {formatDate(profile.createdAt)}</p>
      </div>

      <div className="card">
        <div className="card-title">Personal Details</div>
        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

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
          <button className="btn btn-primary" disabled={loading}>
            {loading ? 'Saving…' : 'Update Profile'}
          </button>
        </form>
      </div>
    </div>
  );
}

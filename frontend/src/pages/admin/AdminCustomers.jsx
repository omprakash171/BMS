import { useEffect, useState } from 'react';
import api, { errorMessage, formatDate, formatINR } from '../../services/api.js';
import StatusBadge from '../../components/StatusBadge.jsx';
import Modal from '../../components/Modal.jsx';

const EMPTY_FORM = {
  username: '', password: '', firstName: '', lastName: '', email: '', phone: '',
  address: '', dateOfBirth: '', accountType: 'SAVINGS', initialDeposit: '0',
};

export default function AdminCustomers() {
  const [customers, setCustomers] = useState([]);
  const [search, setSearch] = useState('');
  const [detail, setDetail] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [notice, setNotice] = useState(null); // {type: 'success'|'error', text}
  const [loading, setLoading] = useState(true);

  async function load(params = {}) {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/customers', { params });
      setCustomers(data);
    } catch (err) {
      setNotice({ type: 'error', text: errorMessage(err) });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function openDetail(id) {
    try {
      const { data } = await api.get(`/admin/customers/${id}`);
      setDetail(data);
    } catch (err) {
      setNotice({ type: 'error', text: errorMessage(err) });
    }
  }

  async function changeStatus(customer, status) {
    setNotice(null);
    try {
      await api.put(`/admin/customers/${customer.id}/status`, { status });
      setNotice({ type: 'success', text: `${customer.firstName} ${customer.lastName} is now ${status}` });
      load(search ? { search } : {});
      if (detail) {
        const { data } = await api.get(`/admin/customers/${customer.id}`);
        setDetail(data);
      }
    } catch (err) {
      setNotice({ type: 'error', text: errorMessage(err) });
    }
  }

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function handleCreate(e) {
    e.preventDefault();
    setNotice(null);
    try {
      const { data } = await api.post('/admin/customers', {
        ...form,
        dateOfBirth: form.dateOfBirth || null,
        initialDeposit: Number(form.initialDeposit || 0),
      });
      setNotice({
        type: 'success',
        text: `Customer created: ${data.customerId}, account ${data.accountNumber}, username ${data.username}`,
      });
      setShowCreate(false);
      setForm(EMPTY_FORM);
      load();
    } catch (err) {
      setNotice({ type: 'error', text: errorMessage(err) });
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Customers</h2>
        <p>Manage bank customers</p>
      </div>

      <div className="card">
        <div className="toolbar">
          <div className="form-group">
            <label>Search</label>
            <input
              className="form-control"
              placeholder="Name, customer ID or email"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && load(search ? { search } : {})}
            />
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => load(search ? { search } : {})}>Search</button>
          <div className="toolbar-spacer" />
          <button className="btn btn-success btn-sm" onClick={() => setShowCreate(true)}>+ New Customer</button>
        </div>

        {notice && <div className={`alert alert-${notice.type}`}>{notice.text}</div>}
        {loading ? (
          <div className="loading">Loading customers…</div>
        ) : (
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Customer ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {customers.map((c) => (
                  <tr key={c.id}>
                    <td className="mono">{c.customerId}</td>
                    <td>{c.firstName} {c.lastName}</td>
                    <td>{c.email}</td>
                    <td>{c.phone}</td>
                    <td><StatusBadge value={c.status} /></td>
                    <td>
                      <div className="actions-cell">
                        <button className="btn btn-outline btn-sm" onClick={() => openDetail(c.id)}>View</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {customers.length === 0 && <p className="empty-state">No customers found.</p>}
          </div>
        )}
      </div>

      {detail && (
        <Modal title={`${detail.customer.firstName} ${detail.customer.lastName} (${detail.customer.customerId})`} onClose={() => setDetail(null)}>
          <dl className="detail-grid">
            <dt>Email</dt><dd>{detail.customer.email}</dd>
            <dt>Phone</dt><dd>{detail.customer.phone}</dd>
            <dt>Address</dt><dd>{detail.customer.address || '-'}</dd>
            <dt>Date of Birth</dt><dd>{formatDate(detail.customer.dateOfBirth)}</dd>
            <dt>Joined</dt><dd>{formatDate(detail.customer.createdAt)}</dd>
            <dt>Status</dt><dd><StatusBadge value={detail.customer.status} /></dd>
          </dl>

          <div className="card-title" style={{ marginTop: 18 }}>Bank Accounts</div>
          {detail.accounts.length === 0 && <p className="empty-state">No accounts.</p>}
          {detail.accounts.map((a) => (
            <div className="summary-box" key={a.accountNumber}>
              <div className="row"><span>Account</span><b className="mono">{a.accountNumber}</b></div>
              <div className="row"><span>Type</span><span>{a.accountType}</span></div>
              <div className="row"><span>Balance</span><b>{formatINR(a.balance)}</b></div>
              <div className="row"><span>Status</span><StatusBadge value={a.status} /></div>
            </div>
          ))}

          <div className="actions-cell" style={{ marginTop: 16 }}>
            {detail.customer.status === 'ACTIVE' ? (
              <button className="btn btn-danger btn-sm" onClick={() => changeStatus(detail.customer, 'INACTIVE')}>
                Deactivate Customer
              </button>
            ) : (
              <button className="btn btn-success btn-sm" onClick={() => changeStatus(detail.customer, 'ACTIVE')}>
                Activate Customer
              </button>
            )}
          </div>
        </Modal>
      )}

      {showCreate && (
        <Modal title="Create Customer" onClose={() => setShowCreate(false)}>
          <form onSubmit={handleCreate}>
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
                <input className="form-control" value={form.username} onChange={update('username')} required />
              </div>
              <div className="form-group">
                <label>Password</label>
                <input type="password" className="form-control" value={form.password} onChange={update('password')} required minLength={6} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Account Type</label>
                <select className="form-control" value={form.accountType} onChange={update('accountType')}>
                  <option value="SAVINGS">Savings</option>
                  <option value="CURRENT">Current</option>
                </select>
              </div>
              <div className="form-group">
                <label>Initial Deposit (₹)</label>
                <input type="number" min="0" step="0.01" className="form-control" value={form.initialDeposit} onChange={update('initialDeposit')} />
              </div>
            </div>
            <div className="actions-cell">
              <button className="btn btn-success" type="submit">Create Customer</button>
              <button className="btn btn-outline" type="button" onClick={() => setShowCreate(false)}>Cancel</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

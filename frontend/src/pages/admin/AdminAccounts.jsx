import { useEffect, useState } from 'react';
import api, { errorMessage, formatDate, formatINR } from '../../services/api.js';
import StatusBadge from '../../components/StatusBadge.jsx';

export default function AdminAccounts() {
  const [accounts, setAccounts] = useState([]);
  const [search, setSearch] = useState('');
  const [notice, setNotice] = useState(null);
  const [loading, setLoading] = useState(true);

  async function load(params = {}) {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/accounts', { params });
      setAccounts(data);
    } catch (err) {
      setNotice({ type: 'error', text: errorMessage(err) });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function changeStatus(account, status) {
    setNotice(null);
    try {
      await api.put(`/admin/accounts/${account.accountNumber}/status`, { status });
      setNotice({
        type: 'success',
        text: `Account ${account.accountNumber} is now ${status}`,
      });
      load(search ? { search } : {});
    } catch (err) {
      setNotice({ type: 'error', text: errorMessage(err) });
    }
  }

  const ACTION_BUTTONS = {
    ACTIVE: { label: 'Activate', className: 'btn-success' },
    BLOCKED: { label: 'Block', className: 'btn-warning' },
    CLOSED: { label: 'Close', className: 'btn-danger' },
  };

  return (
    <div>
      <div className="page-header">
        <h2>Accounts</h2>
        <p>View and manage all bank accounts</p>
      </div>

      <div className="card">
        <div className="toolbar">
          <div className="form-group">
            <label>Search</label>
            <input
              className="form-control"
              placeholder="Account number or customer name"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && load(search ? { search } : {})}
            />
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => load(search ? { search } : {})}>Search</button>
        </div>

        {notice && <div className={`alert alert-${notice.type}`}>{notice.text}</div>}
        {loading ? (
          <div className="loading">Loading accounts…</div>
        ) : (
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Account Number</th>
                  <th>Customer</th>
                  <th>Type</th>
                  <th className="text-right">Balance</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((a) => (
                  <tr key={a.accountNumber}>
                    <td className="mono">{a.accountNumber}</td>
                    <td>
                      {a.customerName}
                      <div className="text-muted text-sm">{a.customerId}</div>
                    </td>
                    <td>{a.accountType}</td>
                    <td className="text-right font-semibold">{formatINR(a.balance)}</td>
                    <td><StatusBadge value={a.status} /></td>
                    <td>{formatDate(a.createdAt)}</td>
                    <td>
                      <div className="actions-cell">
                        {Object.entries(ACTION_BUTTONS)
                          .filter(([status]) => status !== a.status)
                          .map(([status, cfg]) => (
                            <button
                              key={status}
                              className={`btn ${cfg.className} btn-sm`}
                              onClick={() => changeStatus(a, status)}
                            >
                              {cfg.label}
                            </button>
                          ))}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {accounts.length === 0 && <p className="empty-state">No accounts found.</p>}
          </div>
        )}
      </div>
    </div>
  );
}

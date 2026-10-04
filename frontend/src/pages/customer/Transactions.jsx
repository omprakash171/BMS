import { useEffect, useState } from 'react';
import api, { errorMessage } from '../../services/api.js';
import TransactionTable from '../../components/TransactionTable.jsx';

export default function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [filters, setFilters] = useState({ type: '', status: '', startDate: '', endDate: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  async function load(params = {}) {
    setLoading(true);
    setError('');
    try {
      const { data } = await api.get('/transactions', { params });
      setTransactions(data);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  function handleFilterChange(field) {
    return (e) => setFilters({ ...filters, [field]: e.target.value });
  }

  function applyFilters() {
    const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== ''));
    load(params);
  }

  function clearFilters() {
    setFilters({ type: '', status: '', startDate: '', endDate: '' });
    load();
  }

  return (
    <div>
      <div className="page-header">
        <h2>Transaction History</h2>
        <p>All transactions on your accounts</p>
      </div>

      <div className="card">
        <div className="toolbar">
          <div className="form-group">
            <label>Type</label>
            <select className="form-control" value={filters.type} onChange={handleFilterChange('type')}>
              <option value="">All</option>
              <option value="DEPOSIT">Deposit</option>
              <option value="WITHDRAW">Withdraw</option>
              <option value="TRANSFER">Transfer</option>
            </select>
          </div>
          <div className="form-group">
            <label>Status</label>
            <select className="form-control" value={filters.status} onChange={handleFilterChange('status')}>
              <option value="">All</option>
              <option value="SUCCESS">Success</option>
              <option value="FAILED">Failed</option>
            </select>
          </div>
          <div className="form-group">
            <label>From Date</label>
            <input type="date" className="form-control" value={filters.startDate} onChange={handleFilterChange('startDate')} />
          </div>
          <div className="form-group">
            <label>To Date</label>
            <input type="date" className="form-control" value={filters.endDate} onChange={handleFilterChange('endDate')} />
          </div>
          <button className="btn btn-primary btn-sm" onClick={applyFilters}>Apply</button>
          <button className="btn btn-outline btn-sm" onClick={clearFilters}>Clear</button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {loading ? <div className="loading">Loading transactions…</div> : <TransactionTable transactions={transactions} />}
      </div>
    </div>
  );
}

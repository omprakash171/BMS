import { useEffect, useState } from 'react';
import api, { errorMessage } from '../../services/api.js';
import TransactionTable from '../../components/TransactionTable.jsx';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/stats')
      .then((res) => setStats(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!stats) return <div className="loading">Loading dashboard…</div>;

  return (
    <div>
      <div className="page-header">
        <h2>Bank Dashboard</h2>
        <p>Overview of the bank</p>
      </div>

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-label">Total Customers</div>
          <div className="stat-value">{stats.totalCustomers}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Total Accounts</div>
          <div className="stat-value">{stats.totalAccounts}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Active Accounts</div>
          <div className="stat-value">{stats.activeAccounts}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Total Transactions</div>
          <div className="stat-value">{stats.totalTransactions}</div>
        </div>
      </div>

      <div className="card">
        <div className="card-title">Recent Transactions</div>
        <TransactionTable transactions={stats.recentTransactions} showAccount showView={false} />
      </div>
    </div>
  );
}

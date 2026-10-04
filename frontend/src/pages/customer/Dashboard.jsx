import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errorMessage, formatINR } from '../../services/api.js';
import { useAuth } from '../../context/AuthContext.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import TransactionTable from '../../components/TransactionTable.jsx';

export default function Dashboard() {
  const { user } = useAuth();
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [accountRes, txnRes] = await Promise.all([
          api.get('/accounts/my-account'),
          api.get('/transactions'),
        ]);
        setAccount(accountRes.data);
        setTransactions(txnRes.data.slice(0, 5));
      } catch (err) {
        setError(errorMessage(err));
      }
    }
    load();
  }, []);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!account) return <div className="loading">Loading your dashboard…</div>;

  return (
    <div>
      <div className="page-header">
        <h2>Welcome, {account.customerName}</h2>
        <p>Customer ID: {account.customerId}</p>
      </div>

      <div className="stat-grid">
        <div className="stat-card balance-card">
          <div className="stat-label">Available Balance</div>
          <div className="stat-value">{formatINR(account.balance)}</div>
          <div className="balance-meta">
            <span>Account: <b className="mono">{account.accountNumber}</b></span>
            <span>Type: <b>{account.accountType}</b></span>
            <span>Status: <StatusBadge value={account.status} /></span>
          </div>
        </div>
      </div>

      <div className="quick-actions" style={{ marginBottom: 22 }}>
        <Link to="/customer/deposit" className="quick-action"><span className="qa-icon">➕</span>Deposit</Link>
        <Link to="/customer/withdraw" className="quick-action"><span className="qa-icon">➖</span>Withdraw</Link>
        <Link to="/customer/transfer" className="quick-action"><span className="qa-icon">🔁</span>Transfer Money</Link>
      </div>

      <div className="card">
        <div className="card-title">
          Recent Transactions{' '}
          <Link to="/customer/transactions" className="link" style={{ float: 'right', fontWeight: 400 }}>
            View all →
          </Link>
        </div>
        <TransactionTable transactions={transactions} />
      </div>
    </div>
  );
}

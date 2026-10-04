import { useEffect, useState } from 'react';
import api, { errorMessage, formatDate, formatINR } from '../../services/api.js';
import StatusBadge from '../../components/StatusBadge.jsx';

export default function Account() {
  const [account, setAccount] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/accounts/my-account')
      .then((res) => setAccount(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!account) return <div className="loading">Loading account details…</div>;

  return (
    <div>
      <div className="page-header">
        <h2>My Account</h2>
        <p>Your account details</p>
      </div>

      <div className="card">
        <div className="card-title">Account Information</div>
        <dl className="detail-grid">
          <dt>Account Number</dt>
          <dd className="mono">{account.accountNumber}</dd>
          <dt>Account Holder</dt>
          <dd>{account.customerName}</dd>
          <dt>Customer ID</dt>
          <dd className="mono">{account.customerId}</dd>
          <dt>Account Type</dt>
          <dd>{account.accountType}</dd>
          <dt>Available Balance</dt>
          <dd className="font-semibold">{formatINR(account.balance)}</dd>
          <dt>Status</dt>
          <dd><StatusBadge value={account.status} /></dd>
          <dt>Opened On</dt>
          <dd>{formatDate(account.createdAt)}</dd>
        </dl>
      </div>
    </div>
  );
}

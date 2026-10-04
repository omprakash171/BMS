import { useEffect, useState } from 'react';
import api, { errorMessage, formatINR } from '../../services/api.js';

export default function Deposit() {
  const [account, setAccount] = useState(null);
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/accounts/my-account')
      .then((res) => setAccount(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const { data } = await api.post('/accounts/deposit', {
        amount: Number(amount),
        description,
      });
      setSuccess(`${formatINR(data.amount)} deposited successfully. New balance: ${formatINR(data.balanceAfterTransaction)}`);
      setAmount('');
      setDescription('');
      const refreshed = await api.get('/accounts/my-account');
      setAccount(refreshed.data);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ maxWidth: 560 }}>
      <div className="page-header">
        <h2>Deposit Money</h2>
        <p>Add money to your account</p>
      </div>

      <div className="card">
        {account && (
          <p style={{ marginTop: 0 }}>
            Current Balance: <b>{formatINR(account.balance)}</b>{' '}
            <StatusBadgeInline status={account.status} />
          </p>
        )}
        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Amount (₹)</label>
            <input
              type="number"
              min="0.01"
              step="0.01"
              className="form-control"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Description</label>
            <input
              className="form-control"
              placeholder="e.g. Salary, Cash deposit"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <button className="btn btn-success" disabled={loading}>
            {loading ? 'Processing…' : 'Deposit Money'}
          </button>
        </form>
      </div>
    </div>
  );

  function StatusBadgeInline({ status }) {
    return status === 'ACTIVE' ? null : <em style={{ color: 'var(--red)' }}>(account {status.toLowerCase()})</em>;
  }
}

import { useEffect, useState } from 'react';
import api, { errorMessage, formatINR } from '../../services/api.js';

export default function Transfer() {
  const [account, setAccount] = useState(null);
  const [toAccount, setToAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [confirming, setConfirming] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/accounts/my-account')
      .then((res) => setAccount(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  function handleReview(e) {
    e.preventDefault();
    setError('');
    if (toAccount === account?.accountNumber) {
      setError('Sender and receiver accounts must be different');
      return;
    }
    setConfirming(true);
  }

  async function handleConfirm() {
    setLoading(true);
    setError('');
    try {
      const { data } = await api.post('/accounts/transfer', {
        toAccountNumber: toAccount,
        amount: Number(amount),
        description,
      });
      setSuccess(
        `${formatINR(data.amount)} transferred successfully. Transaction ID: ${data.transactionId}. New balance: ${formatINR(data.balanceAfterTransaction)}`
      );
      setConfirming(false);
      setToAccount('');
      setAmount('');
      setDescription('');
      const refreshed = await api.get('/accounts/my-account');
      setAccount(refreshed.data);
    } catch (err) {
      setError(errorMessage(err));
      setConfirming(false);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ maxWidth: 560 }}>
      <div className="page-header">
        <h2>Transfer Money</h2>
        <p>Send money to another account</p>
      </div>

      <div className="card">
        {account && (
          <p style={{ marginTop: 0 }}>
            Your Account: <b className="mono">{account.accountNumber}</b> · Available:{' '}
            <b>{formatINR(account.balance)}</b>
          </p>
        )}
        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        {!confirming ? (
          <form onSubmit={handleReview}>
            <div className="form-group">
              <label>Receiver Account Number</label>
              <input
                className="form-control mono"
                value={toAccount}
                onChange={(e) => setToAccount(e.target.value)}
                required
              />
            </div>
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
                placeholder="e.g. Rent, Gift"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>
            <button className="btn btn-primary">Review Transfer</button>
          </form>
        ) : (
          <div>
            <div className="card-title">Transfer Summary</div>
            <div className="summary-box">
              <div className="row"><span>From</span><b className="mono">{account?.accountNumber}</b></div>
              <div className="row"><span>To</span><b className="mono">{toAccount}</b></div>
              <div className="row"><span>Amount</span><b>{formatINR(Number(amount))}</b></div>
              {description && <div className="row"><span>Description</span><span>{description}</span></div>}
            </div>
            <div className="actions-cell">
              <button className="btn btn-success" onClick={handleConfirm} disabled={loading}>
                {loading ? 'Processing…' : 'Confirm Transfer'}
              </button>
              <button className="btn btn-outline" onClick={() => setConfirming(false)} disabled={loading}>
                Back
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

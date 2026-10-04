import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import api, { errorMessage, formatDateTime, formatINR } from '../../services/api.js';
import StatusBadge from '../../components/StatusBadge.jsx';

export default function TransactionDetail() {
  const { transactionId } = useParams();
  const [txn, setTxn] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(`/transactions/${transactionId}`)
      .then((res) => setTxn(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, [transactionId]);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!txn) return <div className="loading">Loading transaction…</div>;

  return (
    <div style={{ maxWidth: 640 }}>
      <div className="page-header">
        <h2>Transaction Details</h2>
        <p><Link to="/customer/transactions" className="link">← Back to transactions</Link></p>
      </div>

      <div className="card">
        <dl className="detail-grid">
          <dt>Transaction ID</dt>
          <dd className="mono">{txn.transactionId}</dd>
          <dt>Date &amp; Time</dt>
          <dd>{formatDateTime(txn.transactionDate)}</dd>
          <dt>Type</dt>
          <dd><StatusBadge value={txn.transactionType} /></dd>
          <dt>Amount</dt>
          <dd className={`font-semibold ${txn.direction === 'CREDIT' ? 'text-green' : 'text-red'}`}>
            {txn.direction === 'CREDIT' ? '+' : '-'} {formatINR(txn.amount)}
          </dd>
          <dt>Account</dt>
          <dd className="mono">{txn.accountNumber}</dd>
          {txn.referenceAccount && (
            <>
              <dt>{txn.transactionType === 'TRANSFER' ? 'Other Account' : 'Reference'}</dt>
              <dd className="mono">{txn.referenceAccount}</dd>
            </>
          )}
          <dt>Description</dt>
          <dd>{txn.description || '-'}</dd>
          <dt>Balance After</dt>
          <dd>{formatINR(txn.balanceAfterTransaction)}</dd>
          <dt>Status</dt>
          <dd><StatusBadge value={txn.status} /></dd>
        </dl>
      </div>
    </div>
  );
}

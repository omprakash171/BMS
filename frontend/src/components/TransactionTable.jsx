import { Link } from 'react-router-dom';
import StatusBadge from './StatusBadge.jsx';
import { formatDate, formatINR } from '../services/api.js';

export default function TransactionTable({ transactions, showAccount = false, showView = true }) {
  if (!transactions || transactions.length === 0) {
    return <p className="empty-state">No transactions found.</p>;
  }

  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            <th>Transaction ID</th>
            <th>Date</th>
            <th>Type</th>
            {showAccount && <th>Account</th>}
            <th className="text-right">Amount</th>
            <th>Description</th>
            <th>Balance After</th>
            <th>Status</th>
            {showView && <th></th>}
          </tr>
        </thead>
        <tbody>
          {transactions.map((txn) => (
            <tr key={txn.transactionId}>
              <td className="mono">{txn.transactionId}</td>
              <td>{formatDate(txn.transactionDate)}</td>
              <td>
                <StatusBadge value={txn.transactionType} />
              </td>
              {showAccount && <td className="mono">{txn.accountNumber}</td>}
              <td className={`text-right font-semibold ${txn.direction === 'CREDIT' ? 'text-green' : 'text-red'}`}>
                {txn.direction === 'CREDIT' ? '+' : '-'} {formatINR(txn.amount)}
              </td>
              <td>{txn.description || '-'}</td>
              <td>{formatINR(txn.balanceAfterTransaction)}</td>
              <td>
                <StatusBadge value={txn.status} />
              </td>
              {showView && (
                <td>
                  <Link to={`/customer/transactions/${txn.transactionId}`} className="link">
                    View
                  </Link>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

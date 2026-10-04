import { useEffect, useState } from 'react';
import api, { errorMessage } from '../../services/api.js';
import TransactionTable from '../../components/TransactionTable.jsx';

export default function AdminTransactions() {
  const [transactions, setTransactions] = useState([]);
  const [notice, setNotice] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const { data } = await api.get('/admin/transactions');
        setTransactions(data);
      } catch (err) {
        setNotice({ type: 'error', text: errorMessage(err) });
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <div>
      <div className="page-header">
        <h2>Transactions</h2>
        <p>All transactions across the bank</p>
      </div>

      <div className="card">
        {notice && <div className={`alert alert-${notice.type}`}>{notice.text}</div>}
        {loading ? (
          <div className="loading">Loading transactions…</div>
        ) : (
          <TransactionTable transactions={transactions} showAccount showView={false} />
        )}
      </div>
    </div>
  );
}

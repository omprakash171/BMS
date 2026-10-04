import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AppLayout from './components/AppLayout.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import NotFound from './pages/NotFound.jsx';
import CustomerDashboard from './pages/customer/Dashboard.jsx';
import Profile from './pages/customer/Profile.jsx';
import Account from './pages/customer/Account.jsx';
import Deposit from './pages/customer/Deposit.jsx';
import Withdraw from './pages/customer/Withdraw.jsx';
import Transfer from './pages/customer/Transfer.jsx';
import Transactions from './pages/customer/Transactions.jsx';
import TransactionDetail from './pages/customer/TransactionDetail.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import AdminCustomers from './pages/admin/AdminCustomers.jsx';
import AdminAccounts from './pages/admin/AdminAccounts.jsx';
import AdminTransactions from './pages/admin/AdminTransactions.jsx';

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'ADMIN' ? '/admin/dashboard' : '/customer/dashboard'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute role="CUSTOMER" />}>
        <Route element={<AppLayout />}>
          <Route path="/customer/dashboard" element={<CustomerDashboard />} />
          <Route path="/customer/profile" element={<Profile />} />
          <Route path="/customer/account" element={<Account />} />
          <Route path="/customer/deposit" element={<Deposit />} />
          <Route path="/customer/withdraw" element={<Withdraw />} />
          <Route path="/customer/transfer" element={<Transfer />} />
          <Route path="/customer/transactions" element={<Transactions />} />
          <Route path="/customer/transactions/:transactionId" element={<TransactionDetail />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute role="ADMIN" />}>
        <Route element={<AppLayout />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/customers" element={<AdminCustomers />} />
          <Route path="/admin/accounts" element={<AdminAccounts />} />
          <Route path="/admin/transactions" element={<AdminTransactions />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

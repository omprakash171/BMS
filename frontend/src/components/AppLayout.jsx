import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const CUSTOMER_LINKS = [
  { to: '/customer/dashboard', label: 'Dashboard', icon: '🏠' },
  { to: '/customer/account', label: 'My Account', icon: '💳' },
  { to: '/customer/deposit', label: 'Deposit', icon: '➕' },
  { to: '/customer/withdraw', label: 'Withdraw', icon: '➖' },
  { to: '/customer/transfer', label: 'Transfer Money', icon: '🔁' },
  { to: '/customer/transactions', label: 'Transactions', icon: '📄' },
  { to: '/customer/profile', label: 'Profile', icon: '👤' },
];

const ADMIN_LINKS = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: '🏠' },
  { to: '/admin/customers', label: 'Customers', icon: '👥' },
  { to: '/admin/accounts', label: 'Accounts', icon: '💳' },
  { to: '/admin/transactions', label: 'Transactions', icon: '📄' },
];

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const links = user.role === 'ADMIN' ? ADMIN_LINKS : CUSTOMER_LINKS;

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">🏦</span> BMS
        </div>
        <nav>
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              <span className="nav-icon">{link.icon}</span> {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <span className="topbar-title">
            {user.role === 'ADMIN' ? 'Admin Panel' : 'Internet Banking'}
          </span>
          <div className="topbar-user">
            <span className="user-chip">
              {user.username} · <b>{user.role}</b>
            </span>
            <button className="btn btn-outline btn-sm" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

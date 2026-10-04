import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

/**
 * Guards routes. If role is given, only that role may pass;
 * the other role is redirected to its own dashboard.
 */
export default function ProtectedRoute({ role }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (role && user.role !== role) {
    return <Navigate to={user.role === 'ADMIN' ? '/admin/dashboard' : '/customer/dashboard'} replace />;
  }

  return <Outlet />;
}

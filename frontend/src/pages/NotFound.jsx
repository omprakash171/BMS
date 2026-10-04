import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="notfound">
      <h1 style={{ fontSize: 48 }}>404</h1>
      <p>Page not found.</p>
      <Link to="/" className="btn btn-primary">Go Home</Link>
    </div>
  );
}

const STYLES = {
  ACTIVE: 'badge badge-green',
  SUCCESS: 'badge badge-green',
  CREDIT: 'badge badge-green',
  INACTIVE: 'badge badge-gray',
  BLOCKED: 'badge badge-red',
  CLOSED: 'badge badge-dark',
  FAILED: 'badge badge-red',
  DEBIT: 'badge badge-amber',
  PENDING: 'badge badge-amber',
};

export default function StatusBadge({ value }) {
  if (!value) return <span className="badge badge-gray">-</span>;
  const className = STYLES[value.toUpperCase()] || 'badge badge-blue';
  return <span className={className}>{value}</span>;
}

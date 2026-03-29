import type React from 'react';

export function FormGroup({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="form-group">
      <div className="form-heading">
        <label className="form-label">{label}</label>
        {hint ? <span className="form-hint">{hint}</span> : null}
      </div>
      {children}
    </div>
  );
}

export function ModalShell({
  title,
  subtitle,
  onClose,
  children,
  maxWidth,
}: {
  title: string;
  subtitle?: string;
  onClose: () => void;
  children: React.ReactNode;
  maxWidth?: number;
}) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        style={maxWidth ? { maxWidth } : undefined}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div>
            <h2>{title}</h2>
            {subtitle ? <p className="modal-subtitle">{subtitle}</p> : null}
          </div>
          <button type="button" className="icon-button" onClick={onClose} aria-label="Close dialog">
            X
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

export function DataTable({
  headers,
  empty,
  emptyMessage,
  children,
}: {
  headers: string[];
  empty: boolean;
  emptyMessage: string;
  children: React.ReactNode;
}) {
  return (
    <div className="surface-panel table-shell">
      <table className="data-table">
        <thead>
          <tr>{headers.map((header) => <th key={header}>{header}</th>)}</tr>
        </thead>
        <tbody>
          {empty ? (
            <tr>
              <td colSpan={headers.length} className="table-empty">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            children
          )}
        </tbody>
      </table>
    </div>
  );
}

export function MetricCard({
  label,
  value,
  tone = 'default',
}: {
  label: string;
  value: string;
  tone?: 'default' | 'accent' | 'warning';
}) {
  return (
    <div className={`metric-card metric-card-${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

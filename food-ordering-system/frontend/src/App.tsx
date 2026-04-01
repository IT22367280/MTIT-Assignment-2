import type { ReactNode } from 'react';
import { Home, ShieldCheck, Store, Utensils } from 'lucide-react';
import { LandingPage } from './pages/LandingPage';
import { AdminPage } from './pages/AdminPage';
import { ShopPage } from './pages/ShopPage';
import { Toaster } from 'sonner';
import { navigate, usePathname } from './navigation';

type AppRoute = '/' | '/shop' | '/admin';



function coerceRoute(pathname: string): AppRoute | null {
  if (pathname === '/' || pathname === '/shop' || pathname === '/admin') {
    return pathname;
  }

  return null;
}

function NavLink({
  to,
  label,
  icon,
  active,
}: {
  to: AppRoute;
  label: string;
  icon: ReactNode;
  active: boolean;
}) {
  return (
    <a
      href={to}
      className={`nav-link${active ? ' nav-link-active' : ''}`}
      onClick={(event) => {
        if (
          event.defaultPrevented ||
          event.button !== 0 ||
          event.metaKey ||
          event.ctrlKey ||
          event.altKey ||
          event.shiftKey
        ) {
          return;
        }

        event.preventDefault();
        navigate(to);
      }}
    >
      {icon}
      <span>{label}</span>
    </a>
  );
}

export default function App() {
  const pathname = usePathname();
  const route = coerceRoute(pathname);

  return (
    <div className="app-shell">
      <header className="shell-header">
        <div className="shell-brand-block">
          <button type="button" className="shell-brand" onClick={() => navigate('/')}>
            <span className="shell-brand-mark">
              <Utensils size={18} />
            </span>
            <span style={{ fontSize: '1.2rem' }}>
              <strong>FreshBites</strong>
            </span>
          </button>
        </div>

        <nav className="shell-nav" aria-label="Primary">
          <NavLink to="/" label="Home" icon={<Home size={16} />} active={route === '/'} />
          <NavLink to="/shop" label="Shop" icon={<Store size={16} />} active={route === '/shop'} />
          <NavLink
            to="/admin"
            label="Admin"
            icon={<ShieldCheck size={16} />}
            active={route === '/admin'}
          />
        </nav>
      </header>

      <main className="shell-main">
        {!route && (
          <section className="surface-panel page-stack">
            <p className="eyebrow">Unknown path</p>
            <h2>Use one of the supported app routes</h2>
            <p className="body-copy">
              This frontend now exposes a landing page, a customer shop surface, and an admin console.
            </p>
            <div className="inline-actions">
              <button type="button" className="btn btn-primary" onClick={() => navigate('/')}>
                Go Home
              </button>
            </div>
          </section>
        )}
        {route === '/' && <LandingPage />}
        {route === '/shop' && <ShopPage />}
        {route === '/admin' && <AdminPage />}
      </main>
      <Toaster position="top-right" richColors theme="light" expand={true} />
    </div>
  );
}

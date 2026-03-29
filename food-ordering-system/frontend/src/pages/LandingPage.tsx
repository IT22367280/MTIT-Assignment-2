import { ArrowRight, LayoutDashboard, Layers3, ShoppingBag, ShieldCheck } from 'lucide-react';
import { navigate } from '../navigation';

const experiences = [
  {
    title: 'Customer Shop',
    description: 'Public-facing flow for browsing the menu, building a cart, and placing orders.',
    bullets: ['Menu browsing only', 'Checkout isolated from admin tools', 'Built on existing menu, order, and payment services'],
    action: 'Open Shop',
    route: '/shop',
    icon: ShoppingBag,
  },
  {
    title: 'Admin Console',
    description: 'Operational workspace for menu maintenance, customer records, order tracking, and payments.',
    bullets: ['Menu, customer, order, and payment operations', 'Dashboard-style overview', 'Same backend APIs behind the gateway'],
    action: 'Open Admin',
    route: '/admin',
    icon: LayoutDashboard,
  },
];

export function LandingPage() {
  return (
    <div className="page-stack">
      <section className="hero-panel landing-hero">
        <div className="hero-grid">
          <div className="hero-copy">
            <p className="eyebrow">Role-oriented frontend</p>
            <h2 className="hero-title">Customer and admin workflows now live in separate surfaces.</h2>
            <p className="body-copy">
              The backend remains domain-based microservices. This change only separates how each role uses those
              services.
            </p>
            <div className="inline-actions">
              <button type="button" className="btn btn-primary" onClick={() => navigate('/shop')}>
                Start Customer Flow
                <ArrowRight size={16} />
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin')}>
                Open Admin Console
              </button>
            </div>
          </div>
          <div className="hero-metrics">
            <div className="info-card">
              <Layers3 size={18} />
              <strong>Microservices stay intact</strong>
              <p>Customer, menu, order, and payment services remain separate backend capabilities.</p>
            </div>
            <div className="info-card">
              <ShieldCheck size={18} />
              <strong>UI split first</strong>
              <p>This refactor prepares the codebase for real role-based authorization without pretending it exists yet.</p>
            </div>
          </div>
        </div>
      </section>

      <section className="experience-grid">
        {experiences.map(({ title, description, bullets, action, route, icon: Icon }) => (
          <article key={route} className="surface-panel experience-card">
            <div className="experience-heading">
              <span className="experience-icon">
                <Icon size={18} />
              </span>
              <div>
                <h3>{title}</h3>
                <p>{description}</p>
              </div>
            </div>

            <div className="stack-list">
              {bullets.map((bullet) => (
                <div key={bullet} className="list-row">
                  <span className="list-dot" />
                  <span>{bullet}</span>
                </div>
              ))}
            </div>

            <button type="button" className="btn btn-primary experience-action" onClick={() => navigate(route)}>
              {action}
            </button>
          </article>
        ))}
      </section>
    </div>
  );
}

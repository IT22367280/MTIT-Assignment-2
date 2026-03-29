import { useEffect, useState } from 'react';

function normalizePathname(pathname: string): string {
  if (!pathname || pathname === '/index.html') {
    return '/';
  }

  if (pathname.length > 1 && pathname.endsWith('/')) {
    return pathname.slice(0, -1);
  }

  return pathname;
}

export function navigate(pathname: string) {
  const nextPath = normalizePathname(pathname);

  if (normalizePathname(window.location.pathname) === nextPath) {
    return;
  }

  window.history.pushState({}, '', nextPath);
  window.dispatchEvent(new PopStateEvent('popstate'));
}

export function usePathname() {
  const [pathname, setPathname] = useState(() => normalizePathname(window.location.pathname));

  useEffect(() => {
    const handleRouteChange = () => {
      setPathname(normalizePathname(window.location.pathname));
    };

    window.addEventListener('popstate', handleRouteChange);

    return () => {
      window.removeEventListener('popstate', handleRouteChange);
    };
  }, []);

  return pathname;
}

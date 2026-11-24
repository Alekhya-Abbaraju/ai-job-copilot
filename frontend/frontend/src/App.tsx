import React, { Suspense, lazy, useEffect, useState } from 'react';
import JobMatch from './components/JobMatch';

type AuthUser = {
  name?: string;
  email?: string;
  token?: string;
  [key: string]: unknown;
};

type Page =
  | 'dashboard'
  | 'applications'
  | 'resume-workspace'
  | 'ai-match';
type Theme = 'dark' | 'light';

const AuthContainer = lazy(() => import('auth_mfe/AuthContainer'));
const JobsApp = lazy(() =>
  import('jobs_mfe/JobsApp').then((module) => ({
    default: module.default as React.ComponentType<{
      theme: Theme;
      setTheme?: React.Dispatch<React.SetStateAction<Theme>>;
      activePage: 'dashboard' | 'applications';
    }>,
  }))
);const ResumeApp = lazy(() => import('resume_mfe/ResumeApp'));

function App() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [checkingSession, setCheckingSession] = useState(true);
  const [activePage, setActivePage] = useState<Page>('dashboard');

  const [theme, setTheme] = useState<Theme>(() => {
    return localStorage.getItem('theme') === 'light' ? 'light' : 'dark';
  });

  useEffect(() => {
    const savedUser = localStorage.getItem('user');

    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser) as AuthUser);
      } catch {
        localStorage.removeItem('user');
      }
    }

    setCheckingSession(false);
  }, []);

  useEffect(() => {
    localStorage.setItem('theme', theme);
    document.documentElement.classList.toggle('dark', theme === 'dark');
  }, [theme]);

  const handleLoginSuccess = (userData: AuthUser) => {
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
    setActivePage('dashboard');
  };

  if (checkingSession) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950 text-white">
        Checking session...
      </div>
    );
  }

  if (!user) {
    return (
      <Suspense
        fallback={
          <div className="flex min-h-screen items-center justify-center bg-slate-950 text-indigo-400">
            Loading Sign In...
          </div>
        }
      >
        <AuthContainer onLoginSuccess={handleLoginSuccess} />
      </Suspense>
    );
  }

  const isDark = theme === 'dark';

  return (
    <div
      className={`min-h-screen ${
        isDark
          ? 'bg-slate-950 text-white'
          : 'bg-slate-50 text-slate-900'
      }`}
    >
      <header
        className={`flex flex-wrap items-center justify-between gap-4 border-b px-6 py-4 ${
          isDark ? 'border-slate-800' : 'border-slate-200'
        }`}
      >
        <div>
          <h1 className="text-xl font-bold text-indigo-400">
            AI Job Copilot
          </h1>
          <p
            className={`text-sm ${
              isDark ? 'text-slate-400' : 'text-slate-500'
            }`}
          >
            Welcome, {user.name || user.email || 'User'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setTheme(isDark ? 'light' : 'dark')}
            className={`rounded-xl border px-4 py-2 text-sm ${
              isDark
                ? 'border-slate-700 hover:bg-slate-900'
                : 'border-slate-300 hover:bg-slate-100'
            }`}
          >
            {isDark ? '☀️ Light Mode' : '🌙 Dark Mode'}
          </button>

          <button
            onClick={handleLogout}
            className="rounded-xl border border-slate-700 px-4 py-2 text-sm text-red-400 hover:bg-slate-900"
          >
            Sign Out
          </button>
        </div>
      </header>

      <nav
  className={`flex flex-wrap gap-3 border-b px-6 py-3 ${
    isDark ? 'border-slate-800' : 'border-slate-200'
  }`}
>
  {[
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'applications', label: 'My Applications' },
    { id: 'resume-workspace', label: 'Resume Workspace' },
    { id: 'ai-match', label: 'AI Job Match' },
  ].map((page) => (
    <button
      key={page.id}
      onClick={() => setActivePage(page.id as Page)}
      className={`rounded-lg px-4 py-2 text-sm ${
        activePage === page.id
          ? 'bg-indigo-600 text-white'
          : isDark
            ? 'text-slate-300 hover:bg-slate-800'
            : 'text-slate-600 hover:bg-slate-200'
      }`}
    >
      {page.label}
    </button>
  ))}
</nav>

      <main className="p-6">
        <Suspense
          fallback={
            <div
              className={`py-10 text-center ${
                isDark ? 'text-slate-400' : 'text-slate-500'
              }`}
            >
              Loading module...
            </div>
          }
        >
          {activePage === 'dashboard' || activePage === 'applications' ? (
  <JobsApp
    theme={theme}
    setTheme={setTheme}
    activePage={activePage}
  />
) : activePage === 'resume-workspace' ? (
  <ResumeApp
    token={user.token ?? ''}
    theme={theme}
    setTheme={setTheme}
  />
) : (
  <JobMatch
    token={user.token ?? ''}
    theme={theme}
  />
)}
        </Suspense>
      </main>
    </div>
  );
}

export default App;
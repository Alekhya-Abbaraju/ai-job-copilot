
import React, { useState, useEffect } from 'react';
import AuthContainer from './Components/authContainer.jsx';

// Update this path if ResumeApp.jsx is located in another folder.
export default function App() {
    const [user, setUser] = useState(null);
    const [activePage, setActivePage] = useState('dashboard');
    const [theme, setTheme] = useState('dark');

    useEffect(() => {
        const savedUser = localStorage.getItem('user');

        if (savedUser) {
            try {
                setUser(JSON.parse(savedUser));
            } catch (e) {
                localStorage.removeItem('user');
            }
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('user');
        setUser(null);
        setActivePage('dashboard');
    };

    const navigation = [
        { id: 'dashboard', label: 'Dashboard' },
        { id: 'jobs', label: 'My Applications' },
        { id: 'resumes', label: 'Resume Workspace' },
        { id: 'ai-match', label: 'AI Job Match' },
    ];

    if (!user) {
        return (
            <AuthContainer
                onLoginSuccess={(userData) => {
                    localStorage.setItem('user', JSON.stringify(userData));
                    setUser(userData);
                }}
            />
        );
    }

    const renderPage = () => {
        switch (activePage) {
            case 'dashboard':
                return (
                    <>
                        <header className="flex flex-wrap justify-between items-center gap-4 mb-8">
                            <div>
                                <h2 className="text-2xl font-bold">Dashboard</h2>
                                <p className="text-slate-400 text-sm mt-1">
                                    Your job search at a glance.
                                </p>
                            </div>
                            <span className="px-3 py-2 bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs rounded-full font-medium">
                                Welcome, {user.name || 'User'}
                            </span>
                        </header>

                        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
                            {[
                                ['Total Tracked', '3'],
                                ['Interviews Scheduled', '0'],
                                ['Assessments', '0'],
                                ['Offers Received', '0'],
                            ].map(([label, value]) => (
                                <div
                                    key={label}
                                    className="p-6 bg-slate-900 border border-slate-800 rounded-2xl"
                                >
                                    <h3 className="text-slate-400 text-xs uppercase font-semibold tracking-wider mb-3">
                                        {label}
                                    </h3>
                                    <p className="text-3xl font-bold">{value}</p>
                                </div>
                            ))}
                        </div>

                        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                            <div className="flex flex-wrap justify-between items-center gap-3 mb-4">
                                <h3 className="text-lg font-semibold">
                                    Recent Applications
                                </h3>
                                <button
                                    onClick={() => setActivePage('jobs')}
                                    className="text-sm text-indigo-400 hover:text-indigo-300"
                                >
                                    View applications →
                                </button>
                            </div>
                            <p className="text-sm text-slate-400">
                                Track your applications, interviews, and job search progress here.
                            </p>
                        </div>

                        <div className="mt-6 bg-slate-900 border border-slate-800 rounded-2xl p-6">
                            <h3 className="text-lg font-semibold mb-2">
                                Resume Workspace
                            </h3>
                            <p className="text-sm text-slate-400 mb-4">
                                Upload and manage your PDF resumes.
                            </p>
                            <button
                                onClick={() => setActivePage('resumes')}
                                className="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-medium"
                            >
                                Open Resume Workspace
                            </button>
                        </div>
                    </>
                );

            case 'jobs':
                return (
                    <section className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                        <h2 className="text-2xl font-bold mb-2">My Applications</h2>
                        <p className="text-slate-400 text-sm">
                            Connect your Jobs MFE here to display your saved application records.
                        </p>
                    </section>
                );

            case 'resumes':
                return (
                    <ResumeApp
                        token={user.token}
                        theme={theme}
                        setTheme={setTheme}
                    />
                );

            case 'ai-match':
                return (
                    <section className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                        <h2 className="text-2xl font-bold mb-2">AI Job Match</h2>
                        <p className="text-slate-400 text-sm">
                            Your AI job matching component can be connected here.
                        </p>
                    </section>
                );

            default:
                return null;
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white flex">
            <aside className="w-64 shrink-0 border-r border-slate-800 p-6 flex flex-col justify-between min-h-screen">
                <div>
                    <h1 className="text-xl font-bold tracking-wider text-indigo-400 mb-8">
                        AI Job Copilot
                    </h1>

                    <p className="text-xs uppercase tracking-wider text-slate-500 mb-3 px-3">
                        Workspace
                    </p>

                    <nav className="space-y-2">
                        {navigation.map((item) => (
                            <button
                                key={item.id}
                                onClick={() => setActivePage(item.id)}
                                className={`w-full text-left block px-4 py-3 rounded-xl text-sm transition-all ${
                                    activePage === item.id
                                        ? 'bg-indigo-600/15 text-indigo-300 border border-indigo-500/20 font-medium'
                                        : 'text-slate-400 hover:bg-slate-900 hover:text-white'
                                }`}
                            >
                                {item.label}
                            </button>
                        ))}
                    </nav>
                </div>

                <div>
                    <div className="p-3 bg-slate-900 rounded-xl border border-slate-800 mb-4">
                        <p className="text-xs font-semibold text-white">
                            {user.name || 'User'}
                        </p>
                        <p className="text-xs text-slate-400 truncate">
                            {user.email}
                        </p>
                    </div>

                    <button
                        onClick={handleLogout}
                        className="w-full text-left px-4 py-2 text-xs font-semibold text-red-400 hover:bg-red-500/10 rounded-xl transition-all"
                    >
                        Sign Out
                    </button>
                </div>
            </aside>

            <main className="flex-1 min-w-0 p-6 md:p-10 overflow-y-auto">
                {renderPage()}
            </main>
        </div>
    );
}
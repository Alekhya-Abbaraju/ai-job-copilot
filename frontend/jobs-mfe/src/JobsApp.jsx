import './index.css'
import { useEffect, useMemo, useState } from 'react'
import {
  Briefcase, CheckCircle2, Clock, Plus, Search,
  XCircle, AlertCircle, MapPin, TrendingUp, X
} from 'lucide-react'

const API_BASE_URL = 'http://localhost:8080/api/jobs'

const STATUS_CONFIG = {
  APPLIED: { label: 'Applied', style: 'bg-blue-500/10 text-blue-600 border-blue-500/20', icon: Clock },
  ASSESSMENT: { label: 'Assessment', style: 'bg-amber-500/10 text-amber-600 border-amber-500/20', icon: AlertCircle },
  INTERVIEW: { label: 'Interview', style: 'bg-purple-500/10 text-purple-600 border-purple-500/20', icon: TrendingUp },
  OFFER: { label: 'Offer', style: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20', icon: CheckCircle2 },
  REJECTED: { label: 'Rejected', style: 'bg-rose-500/10 text-rose-600 border-rose-500/20', icon: XCircle }
}

export default function JobsApp({
  theme: appTheme = 'dark',
  activePage = 'dashboard'
}) {
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)


function getAuthToken() {
  const keys = ['accessToken', 'token', 'authToken', 'jwt'];

  for (const key of keys) {
    const value =
      localStorage.getItem(key) ||
      sessionStorage.getItem(key);

    if (value) {
      return value.replace(/^Bearer\s+/i, '');
    }
  }

  // Your login flow stores the token inside the "user" object.
  const storedUser =
    localStorage.getItem('user') ||
    sessionStorage.getItem('user');

  if (storedUser) {
    try {
      const user = JSON.parse(storedUser);

      if (user?.token) {
        return user.token.replace(/^Bearer\s+/i, '');
      }
    } catch (error) {
      console.error('Failed to parse stored user.');
    }
  }

  return null;
}
  // Fetch jobs from Spring Boot backend on mount

const fetchJobs = async () => {
  setLoading(true);
  setError('');

  try {
    const token = getAuthToken();

    if (!token) {
      throw new Error('No authentication token found. Please sign in again.');
    }

    const res = await fetch(API_BASE_URL, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!res.ok) {
      const details = await res.text();
      throw new Error(
        `API returned ${res.status}: ${details || res.statusText}`
      );
    }

    const data = await res.json();
    setJobs(data);
  } catch (err) {
    console.error('Failed to fetch job applications:', err);
    setError(
      err instanceof Error
        ? err.message
        : 'Unable to load job applications.'
    );
  } finally {
    setLoading(false);
  }
};
  useEffect(() => {
    fetchJobs()
  }, [])

  // Create job application via POST API

const handleAddJob = async (newJobData) => {
  const token = getAuthToken();

  if (!token) {
    throw new Error('No authentication token found. Please sign in again.');
  }

  const res = await fetch(API_BASE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(newJobData),
  });

  if (!res.ok) {
    const details = await res.text();
    throw new Error(
      `API returned ${res.status}: ${details || res.statusText}`
    );
  }

  await fetchJobs();
  setIsAddModalOpen(false);
};

  const filteredJobs = useMemo(() => {
    return jobs.filter((job) => {
      const query = search.trim().toLowerCase()
      const matchesSearch = [job.companyName, job.jobTitle, job.location].some((val) =>
        (val || '').toLowerCase().includes(query)
      )
      return matchesSearch && (statusFilter === 'ALL' || job.status === statusFilter)
    })
  }, [jobs, search, statusFilter])

  const stats = useMemo(() => {
    return {
      total: jobs.length,
      interviews: jobs.filter((j) => j.status === 'INTERVIEW').length,
      offers: jobs.filter((j) => j.status === 'OFFER').length,
      assessments: jobs.filter((j) => j.status === 'ASSESSMENT').length
    }
  }, [jobs])

  const isDark = appTheme === 'dark'

const uiTheme = isDark
  ? {
      bg: 'bg-slate-950 text-slate-100',
      card: 'bg-slate-900/70 border border-slate-800/80 backdrop-blur-xl',
      cardHover: 'hover:border-slate-700',
      textMuted: 'text-slate-400',
      input: 'bg-slate-900 border border-slate-800 text-slate-100 placeholder:text-slate-500 focus:border-indigo-500'
    }
  : {
      bg: 'bg-slate-50 text-slate-900',
      card: 'bg-white/80 border border-slate-200/80 backdrop-blur-xl shadow-sm',
      cardHover: 'hover:border-slate-300',
      textMuted: 'text-slate-500',
      input: 'bg-white border border-slate-200 text-slate-900 placeholder:text-slate-400 focus:border-indigo-500'
    }

  return (
  <div className={`w-full rounded-3xl overflow-hidden font-sans ${uiTheme.bg}`}>
    <main className="min-w-0 flex flex-col">
      <header className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 px-6 py-4">
        <div>
          <p className={`text-xs ${uiTheme.textMuted}`}>
            {activePage === 'dashboard' ? 'Overview' : 'Application Tracker'}
          </p>

          <h2 className="text-xl font-bold tracking-tight">
            {activePage === 'dashboard' ? 'Dashboard' : 'My Applications'}
          </h2>
        </div>

        <button
          onClick={() => setIsAddModalOpen(true)}
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-xs font-semibold text-white shadow-md shadow-indigo-600/20 transition hover:bg-indigo-500"
        >
          <Plus className="h-4 w-4" />
          Add Application
        </button>
      </header>

      <div className="max-w-7xl w-full mx-auto space-y-6 p-6">

        {error && (
          <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 p-4 text-xs text-rose-400">
            {error}
          </div>
        )}

        {activePage === 'dashboard' && (
          <>
            <div className="jobs-stats-grid">
              {[
                {
                  title: 'Total Tracked',
                  val: stats.total,
                  icon: Briefcase,
                  color: 'text-indigo-500 bg-indigo-500/10'
                },
                {
                  title: 'Interviews Scheduled',
                  val: stats.interviews,
                  icon: TrendingUp,
                  color: 'text-purple-500 bg-purple-500/10'
                },
                {
                  title: 'Assessments',
                  val: stats.assessments,
                  icon: AlertCircle,
                  color: 'text-amber-500 bg-amber-500/10'
                },
                {
                  title: 'Offers Received',
                  val: stats.offers,
                  icon: CheckCircle2,
                  color: 'text-emerald-500 bg-emerald-500/10'
                }
              ].map((s) => (
                <div
                  key={s.title}
                  className={`rounded-2xl p-5 transition ${uiTheme.card} ${uiTheme.cardHover}`}
                >
                  <div className="flex items-center justify-between">
                    <span className={`text-xs font-medium ${uiTheme.textMuted}`}>
                      {s.title}
                    </span>

                    <div className={`flex h-9 w-9 items-center justify-center rounded-xl ${s.color}`}>
                      <s.icon className="h-4 w-4" />
                    </div>
                  </div>

                  <p className="mt-3 text-2xl font-bold">{s.val}</p>
                </div>
              ))}
            </div>

            <div className={`overflow-hidden rounded-2xl ${uiTheme.card}`}>
              <div className="flex flex-col gap-4 border-b border-slate-800 p-5 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h3 className="font-semibold text-base">
                    Recent Applications
                  </h3>

                  <p className={`text-xs ${uiTheme.textMuted}`}>
                    Your latest job application records.
                  </p>
                </div>

                <FilterBar
                  search={search}
                  setSearch={setSearch}
                  filter={statusFilter}
                  setFilter={setStatusFilter}
                  theme={uiTheme}
                />
              </div>

              {loading ? (
                <div className={`p-8 text-center text-xs ${uiTheme.textMuted}`}>
                  Loading jobs from database...
                </div>
              ) : (
                <JobsTable
                  jobs={filteredJobs.slice(0, 5)}
                  theme={uiTheme}
                />
              )}
            </div>
          </>
        )}

        {activePage === 'applications' && (
          <div className={`overflow-hidden rounded-2xl ${uiTheme.card}`}>
            <div className="flex flex-col gap-4 border-b border-slate-800 p-5 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 className="font-semibold text-base">
                  All Applications
                </h3>

                <p className={`text-xs ${uiTheme.textMuted}`}>
                  Full inventory of your job applications.
                </p>
              </div>

              <FilterBar
                search={search}
                setSearch={setSearch}
                filter={statusFilter}
                setFilter={setStatusFilter}
                theme={uiTheme}
              />
            </div>

            {loading ? (
              <div className={`p-8 text-center text-xs ${uiTheme.textMuted}`}>
                Loading jobs from database...
              </div>
            ) : (
              <JobsTable
                jobs={filteredJobs}
                theme={uiTheme}
              />
            )}
          </div>
        )}
      </div>
    </main>

    {isAddModalOpen && (
      <AddJobModal
        theme={uiTheme}
        onClose={() => setIsAddModalOpen(false)}
        onAdd={handleAddJob}
      />
    )}
  </div>
)
}

function FilterBar({ search, setSearch, filter, setFilter, theme }) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <div className="relative min-w-[200px]">
        <Search className={`absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 ${theme.textMuted}`} />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search role or company..."
          className={`w-full rounded-xl border py-1.5 pl-9 pr-3 text-xs outline-none transition ${theme.input}`}
        />
      </div>
      <select
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        className={`rounded-xl border px-3 py-1.5 text-xs outline-none transition ${theme.input}`}
      >
        <option value="ALL">All Statuses</option>
        <option value="APPLIED">Applied</option>
        <option value="ASSESSMENT">Assessment</option>
        <option value="INTERVIEW">Interview</option>
        <option value="OFFER">Offer</option>
        <option value="REJECTED">Rejected</option>
      </select>
    </div>
  )
}

function JobsTable({ jobs, theme }) {
  if (!jobs.length) {
    return <div className={`p-10 text-center text-xs ${theme.textMuted}`}>No job applications found in database.</div>
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-xs">
        <thead className="border-b border-inherit bg-slate-500/5">
          <tr className={theme.textMuted}>
            <th className="px-5 py-3 font-medium">Company</th>
            <th className="px-5 py-3 font-medium">Role</th>
            <th className="px-5 py-3 font-medium">Location</th>
            <th className="px-5 py-3 font-medium">Applied Date</th>
            <th className="px-5 py-3 font-medium">Status</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-inherit">
          {jobs.map((job) => {
            const statusInfo = STATUS_CONFIG[job.status] || STATUS_CONFIG.APPLIED
            const StatusIcon = statusInfo.icon

            return (
              <tr key={job.id} className="transition hover:bg-slate-500/5">
                <td className="whitespace-nowrap px-5 py-3.5">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-indigo-500/10 font-bold text-indigo-500">
                      {job.companyName?.charAt(0) || 'C'}
                    </div>
                    <span className="font-semibold">{job.companyName}</span>
                  </div>
                </td>
                <td className="whitespace-nowrap px-5 py-3.5 font-medium">{job.jobTitle}</td>
                <td className={`whitespace-nowrap px-5 py-3.5 ${theme.textMuted}`}>
                  <div className="flex items-center gap-1">
                    <MapPin className="h-3 w-3" />
                    {job.location || 'Remote'}
                  </div>
                </td>
                <td className={`whitespace-nowrap px-5 py-3.5 ${theme.textMuted}`}>{job.applicationDate}</td>
                <td className="whitespace-nowrap px-5 py-3.5">
                  <span className={`inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-[11px] font-medium ${statusInfo.style}`}>
                    <StatusIcon className="h-3 w-3" />
                    {statusInfo.label}
                  </span>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

function AddJobModal({ theme, onClose, onAdd }) {
  const [companyName, setCompanyName] = useState('')
  const [jobTitle, setJobTitle] = useState('')
  const [jobDescription, setJobDescription] = useState('')
  const [location, setLocation] = useState('')
  const [status, setStatus] = useState('APPLIED')

  const handleSubmit = (e) => {
    e.preventDefault()
    onAdd({
      companyName,
      jobTitle,
      jobDescription,
      location: location || 'Remote',
      status
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className={`w-full max-w-md rounded-2xl border p-6 shadow-2xl ${theme.card}`}>
        <div className="flex items-center justify-between border-b border-inherit pb-4">
          <h3 className="font-bold text-base">Add Application to Database</h3>
          <button onClick={onClose} className={`rounded-lg p-1 transition hover:bg-slate-500/10 ${theme.textMuted}`}>
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4 text-xs">
          <div>
            <label className="block mb-1 font-medium">Company Name *</label>
            <input
              required
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              placeholder="e.g. Microsoft"
              className={`w-full rounded-xl border p-2.5 outline-none ${theme.input}`}
            />
          </div>

          <div>
            <label className="block mb-1 font-medium">Job Title *</label>
            <input
              required
              value={jobTitle}
              onChange={(e) => setJobTitle(e.target.value)}
              placeholder="e.g. Software Engineer"
              className={`w-full rounded-xl border p-2.5 outline-none ${theme.input}`}
            />
          </div>

          <div>
            <label className="block mb-1 font-medium">Job Description</label>
            <textarea
              rows={2}
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              placeholder="Paste job details..."
              className={`w-full rounded-xl border p-2.5 outline-none ${theme.input}`}
            />
          </div>

          <div>
            <label className="block mb-1 font-medium">Location</label>
            <input
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="e.g. Hyderabad, IN (Remote)"
              className={`w-full rounded-xl border p-2.5 outline-none ${theme.input}`}
            />
          </div>

          <div>
            <label className="block mb-1 font-medium">Status</label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className={`w-full rounded-xl border p-2.5 outline-none ${theme.input}`}
            >
              <option value="APPLIED">Applied</option>
              <option value="ASSESSMENT">Assessment</option>
              <option value="INTERVIEW">Interview</option>
              <option value="OFFER">Offer</option>
            </select>
          </div>

          <div className="mt-6 flex justify-end gap-2 pt-2 border-t border-inherit">
            <button type="button" onClick={onClose} className={`rounded-xl border px-4 py-2 font-semibold ${theme.textMuted}`}>
              Cancel
            </button>
            <button type="submit" className="rounded-xl bg-indigo-600 px-4 py-2 font-semibold text-white shadow-md shadow-indigo-600/20 hover:bg-indigo-500">
              Save to Database
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
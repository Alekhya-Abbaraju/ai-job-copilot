import { useCallback, useEffect, useRef, useState } from 'react';
import axios from 'axios';
import {
  FileText,
  Upload,
  RefreshCw,
  CheckCircle2,
  AlertCircle,
  FileUp,
  Clock3,
  HardDrive,
  ShieldCheck,
  Sparkles,
  X,
  Eye,
  Trash2
} from 'lucide-react';
import { createPortal } from 'react-dom';
const API_URL = 'http://localhost:8082/api/resumes';

const api = axios.create({
  baseURL: API_URL,
  timeout: 180000,
});

function getStoredToken() {
  const keys = ['accessToken', 'token', 'authToken', 'jwt'];

  for (const key of keys) {
    const token =
      localStorage.getItem(key) || sessionStorage.getItem(key);

    if (token) {
      return token.replace(/^Bearer\s+/i, '');
    }
  }

  return '';
}

function formatFileSize(bytes = 0) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function formatDate(value) {
  if (!value) return 'Date unavailable';

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return 'Date unavailable';

  return date.toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

function getErrorMessage(error) {
  if (error.response?.status === 401) {
    return 'Your session is invalid or expired. Please sign in again.';
  }

  if (error.response?.status === 403) {
    return 'You are not authorized to access these resumes.';
  }

  if (error.response?.data?.message) {
    return error.response.data.message;
  }

  if (error.code === 'ECONNABORTED') {
    return 'The request timed out. Please try again.';
  }

  if (!error.response) {
    return 'Cannot connect to Resume Service. Check that the backend is running on port 8082.';
  }

  return `Request failed with status ${error.response.status}. Please try again.`;
}

function StatusBadge({ status }) {
  const uploaded = status?.toUpperCase() === 'UPLOADED';

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold ${uploaded
          ? 'bg-emerald-500/10 text-emerald-500 ring-1 ring-emerald-500/30'
          : 'bg-amber-500/10 text-amber-500 ring-1 ring-amber-500/30'
        }`}
    >
      <span
        className={`h-1.5 w-1.5 rounded-full ${uploaded ? 'bg-emerald-500' : 'bg-amber-500'
          }`}
      />
      {status || 'UNKNOWN'}
    </span>
  );
}
function ResumeCard({
  resume,
  darkMode,
  mutedClass,
  onView,
  onDelete,
  isLoading,
  isDeleting,
}) {
  return (
    <article
      className={`group rounded-2xl border p-5 transition-all duration-200 hover:-translate-y-1 hover:shadow-lg ${
        darkMode
          ? 'border-slate-800 bg-slate-900 hover:border-indigo-500/50'
          : 'border-slate-200 bg-white hover:border-indigo-300'
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-red-500/10 text-red-500">
          <FileText size={25} />
        </div>

        <StatusBadge status={resume.status} />
      </div>

      <h3
        title={resume.originalFilename}
        className="mt-5 break-words text-sm font-bold leading-6"
      >
        {resume.originalFilename || 'Untitled resume'}
      </h3>

      <div className={`mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs ${mutedClass}`}>
        <span className="flex items-center gap-1.5">
          <HardDrive size={14} />
          {formatFileSize(Number(resume.fileSize) || 0)}
        </span>

        <span className="flex items-center gap-1.5">
          <Clock3 size={14} />
          {formatDate(resume.createdAt)}
        </span>
      </div>

      <div
        className={`mt-5 flex flex-wrap items-center justify-between gap-3 border-t pt-4 ${
          darkMode ? 'border-slate-800' : 'border-slate-100'
        }`}
      >
        <span className={`text-xs ${mutedClass}`}>
          Resume ID: {resume.id}
        </span>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => onDelete(resume)}
            disabled={isDeleting || isLoading}
            className="inline-flex items-center gap-2 rounded-lg border border-red-500/30 bg-red-500/10 px-3 py-2 text-xs font-semibold text-red-500 transition hover:bg-red-500/20 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Trash2 size={15} />
            {isDeleting ? 'Deleting...' : 'Delete'}
          </button>

          <button
            type="button"
            onClick={() => onView(resume)}
            disabled={isLoading || isDeleting}
            className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-3 py-2 text-xs font-semibold text-white transition hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Eye size={15} />
            {isLoading ? 'Loading...' : 'View Resume'}
          </button>
        </div>
      </div>
    </article>
  );
}
export default function ResumeApp({
  token: tokenProp,
  theme = 'dark',
  setTheme,
}) {
  const darkMode = theme === 'dark';
  const fileInputRef = useRef(null);

  const [resumes, setResumes] = useState([]);
  const [deletingResumeId, setDeletingResumeId] = useState(null);
  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [analysis, setAnalysis] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisStatus, setAnalysisStatus] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [activeTab, setActiveTab] = useState('my-resumes');
  const [previewUrl, setPreviewUrl] = useState(null);
  const [previewFilename, setPreviewFilename] = useState('');
  const [previewLoadingId, setPreviewLoadingId] = useState(null);

  const token = tokenProp || getStoredToken();

  // Reusable theme styles
  const cardClass = darkMode
    ? 'border-slate-800 bg-slate-900 text-slate-100'
    : 'border-slate-200 bg-white text-slate-900';

  const mutedClass = darkMode
    ? 'text-slate-400'
    : 'text-slate-500';

  const subtlePanelClass = darkMode
    ? 'border-slate-700 bg-slate-800'
    : 'border-slate-200 bg-slate-50';

  const secondaryButtonClass = darkMode
    ? 'border-slate-700 bg-slate-900 text-slate-200 hover:bg-slate-800'
    : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-100';

  const loadResumes = useCallback(async () => {
    if (!token) {
      setLoading(false);
      setError('Please sign in to view your resumes.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await api.get('', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setResumes(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    loadResumes();
  }, [loadResumes]);
  async function handleViewResume(resume) {
  if (!token) {
    setError('Please sign in to view your resume.');
    return;
  }

  setPreviewLoadingId(resume.id);
  setError('');

  try {
    const response = await api.get(`/${resume.id}/preview`, {
      responseType: 'blob',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const pdfBlob = new Blob([response.data], {
      type: response.headers['content-type'] || 'application/pdf',
    });

    const objectUrl = URL.createObjectURL(pdfBlob);

    setPreviewFilename(resume.originalFilename || 'Resume.pdf');
    setPreviewUrl(objectUrl);
  } catch (err) {
    setError(getErrorMessage(err));
  } finally {
    setPreviewLoadingId(null);
  }
}
async function handleDeleteResume(resume) {
  const confirmed = window.confirm(
    `Are you sure you want to delete "${resume.originalFilename}"?\n\nThis action cannot be undone.`
  );

  if (!confirmed) return;

  if (!token) {
    setError('Please sign in to delete your resume.');
    return;
  }

  setDeletingResumeId(resume.id);
  setError('');
  setNotice('');

  try {
    await api.delete(`/${resume.id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    setResumes((previousResumes) =>
      previousResumes.filter((item) => item.id !== resume.id)
    );

    if (String(selectedResumeId) === String(resume.id)) {
      setSelectedResumeId('');
      setAnalysis(null);
    }

    setNotice('Resume deleted successfully.');
  } catch (err) {
    setError(getErrorMessage(err));
  } finally {
    setDeletingResumeId(null);
  }
}
async function handleAnalyzeResume() {
  if (!token) {
    setError('Please sign in to analyze your resume.');
    return;
  }

  if (!selectedResumeId) {
    setError('Please select a resume first.');
    return;
  }

  setAnalyzing(true);
  setAnalysisStatus('');
  setError('');
  setNotice('');
  setAnalysis(null);

  try {
    // 1. Submit analysis request
    const response = await api.post(
      `/${selectedResumeId}/analyze`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    const analysisId = response.data.analysisId;

    // 2. Set initial status
    setAnalysisStatus(response.data.status || 'PENDING');

    const maxAttempts = 120; // 10 minutes
    let completed = false;

    for (let attempt = 0; attempt < maxAttempts; attempt++) {
      await new Promise((resolve) => setTimeout(resolve, 5000));

      const resultResponse = await api.get(
        `/analyses/${analysisId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const result = resultResponse.data;
      const status = result.status?.toUpperCase();

      setAnalysisStatus(status);

      if (status === 'COMPLETED') {
        setAnalysis(result);
        setNotice('Resume analysis completed successfully.');
        completed = true;
        break;
      }

      if (status === 'FAILED') {
        throw new Error(
          result.errorMessage || 'AI resume analysis failed.'
        );
      }
    }
    if (!completed) {
      setNotice(
        'Analysis is still processing. Please check again shortly.'
      );
    }

  } catch (err) {
    if (err.response) {
      setError(getErrorMessage(err));
    } else {
      setError(err.message || 'Resume analysis failed.');
    }
  } finally {
    setAnalyzing(false);
  }
}
useEffect(() => {
  return () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
  };
}, [previewUrl]);

  function handleFileChange(event) {
    const file = event.target.files?.[0];

    setError('');
    setNotice('');

    if (!file) {
      setSelectedFile(null);
      return;
    }

    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setSelectedFile(null);
      setError('Please select a PDF resume.');
      event.target.value = '';
      return;
    }

    if (file.size === 0) {
      setSelectedFile(null);
      setError('The selected file is empty.');
      event.target.value = '';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setSelectedFile(null);
      setError('File size must be 5 MB or less.');
      event.target.value = '';
      return;
    }

    setSelectedFile(file);
  }

  async function handleUpload(event) {

    if (!token) {
      setError('Please sign in before uploading a resume.');
      return;
    }

    if (!selectedFile) {
      setError('Choose a PDF file before uploading.');
      return;
    }

    setUploading(true);
    setError('');
    setNotice('');

    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      await api.post('', formData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setSelectedFile(null);

      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }

      setNotice('Resume uploaded successfully.');
      await loadResumes();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setUploading(false);
    }
  }

  const totalSize = resumes.reduce(
    (sum, resume) => sum + (Number(resume.fileSize) || 0),
    0
  );

  return (
    <main
      className={`min-h-screen rounded-2xl transition-colors duration-300 ${darkMode
          ? 'bg-slate-950 text-slate-100'
          : 'bg-slate-50 text-slate-900'
        }`}
    >
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        {/* Header */}
        <header className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <div
              className={`mb-3 inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-semibold ${darkMode
                  ? 'border-indigo-500/30 bg-indigo-500/10 text-indigo-300'
                  : 'border-indigo-100 bg-white text-indigo-700'
                }`}
            >
              <Sparkles size={14} />
              YOUR CAREER WORKSPACE
            </div>

            <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
              Resume Workspace
            </h1>

            <p className={`mt-2 max-w-2xl text-sm leading-6 sm:text-base ${mutedClass}`}>
              Manage your resumes, improve your content and prepare for your next opportunity.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={() => setTheme?.(darkMode ? 'light' : 'dark')}
              className={`inline-flex items-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-semibold shadow-sm transition ${secondaryButtonClass}`}
              aria-label={darkMode ? 'Switch to light mode' : 'Switch to dark mode'}
            >
              <span>{darkMode ? '☀️' : '🌙'}</span>
              {darkMode ? 'Light Mode' : 'Dark Mode'}
            </button>

            <button
              type="button"
              onClick={loadResumes}
              disabled={loading}
              className={`inline-flex items-center justify-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-semibold shadow-sm transition disabled:cursor-not-allowed disabled:opacity-50 ${secondaryButtonClass}`}
            >
              <RefreshCw
                size={16}
                className={loading ? 'animate-spin' : ''}
              />
              Refresh
            </button>
          </div>
        </header>
        {/* Resume Workspace Tabs */}
        <div className={`mb-8 flex flex-wrap gap-2 border-b pb-4 ${darkMode ? 'border-slate-800' : 'border-slate-200'
          }`}>
          {[
            { id: 'my-resumes', label: 'My Resumes' },
            { id: 'upload', label: 'Upload Resume' },
            { id: 'improve', label: 'Improve Resume' },
          ].map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => {
                setActiveTab(tab.id);
                setError('');
                setNotice('');
              }}
              className={`rounded-xl px-4 py-2.5 text-sm font-semibold transition ${activeTab === tab.id
                  ? 'bg-indigo-600 text-white'
                  : secondaryButtonClass
                }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        {activeTab === 'my-resumes' && (
          <div>
            {/* Summary cards */}
            <section className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <div className={`rounded-2xl border p-5 shadow-sm ${cardClass}`}>
                <div className="flex items-center justify-between">
                  <p className={`text-sm font-medium ${mutedClass}`}>
                    Total resumes
                  </p>
                  <div className="rounded-xl bg-indigo-500/10 p-2.5 text-indigo-500">
                    <FileText size={20} />
                  </div>
                </div>
                <p className="mt-4 text-3xl font-bold">
                  {loading ? '—' : resumes.length}
                </p>
                <p className={`mt-1 text-xs ${mutedClass}`}>
                  Resumes in your workspace
                </p>
              </div>

              <div className={`rounded-2xl border p-5 shadow-sm ${cardClass}`}>
                <div className="flex items-center justify-between">
                  <p className={`text-sm font-medium ${mutedClass}`}>
                    Storage used
                  </p>
                  <div className="rounded-xl bg-violet-500/10 p-2.5 text-violet-500">
                    <HardDrive size={20} />
                  </div>
                </div>
                <p className="mt-4 text-3xl font-bold">
                  {loading ? '—' : formatFileSize(totalSize)}
                </p>
                <p className={`mt-1 text-xs ${mutedClass}`}>
                  Combined file sizes
                </p>
              </div>

              <div className={`rounded-2xl border p-5 shadow-sm sm:col-span-2 lg:col-span-1 ${cardClass}`}>
                <div className="flex items-center justify-between">
                  <p className={`text-sm font-medium ${mutedClass}`}>
                    Privacy
                  </p>
                  <div className="rounded-xl bg-emerald-500/10 p-2.5 text-emerald-500">
                    <ShieldCheck size={20} />
                  </div>
                </div>
                <p className="mt-4 text-xl font-bold">Account protected</p>
                <p className={`mt-1 text-xs ${mutedClass}`}>
                  Resumes are retrieved using your authenticated session.
                </p>
              </div>
            </section>
          </div>
        )}

        {/* Error and success messages */}
        {error && (
          <div
            role="alert"
            className={`mb-5 flex items-start gap-3 rounded-xl border p-4 text-sm ${darkMode
                ? 'border-red-900 bg-red-950/50 text-red-300'
                : 'border-red-200 bg-red-50 text-red-800'
              }`}
          >
            <AlertCircle size={19} className="mt-0.5 shrink-0" />
            <p className="flex-1">{error}</p>
            <button
              type="button"
              onClick={() => setError('')}
              aria-label="Dismiss error"
            >
              <X size={17} />
            </button>
          </div>
        )}

        {notice && (
          <div
            role="status"
            className={`mb-5 flex items-center gap-3 rounded-xl border p-4 text-sm ${darkMode
                ? 'border-emerald-900 bg-emerald-950/50 text-emerald-300'
                : 'border-emerald-200 bg-emerald-50 text-emerald-800'
              }`}
          >
            <CheckCircle2 size={19} />
            {notice}
          </div>
        )}
        {activeTab === 'upload' && (
          <div>
            {/* Upload and information panels */}
            <section className="mb-8 grid items-start gap-6 lg:grid-cols-[1.1fr_0.9fr]">
              <div className={`rounded-2xl border p-5 shadow-sm sm:p-6 ${cardClass}`}>
                <div className="mb-5 flex items-center gap-3">
                  <div className="rounded-xl bg-indigo-500/10 p-3 text-indigo-500">
                    <Upload size={21} />
                  </div>
                  <div>
                    <h2 className="text-lg font-bold">Upload a resume</h2>
                    <p className={`mt-1 text-sm ${mutedClass}`}>
                      Add a PDF to your personal workspace.
                    </p>
                  </div>
                </div>

                <div className="space-y-4">
                  <input
                    ref={fileInputRef}
                    id="resume-file"
                    type="file"
                    accept=".pdf,application/pdf"
                    onChange={handleFileChange}
                    className="hidden"
                  />

                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploading}
                    className={`flex w-full items-center justify-center gap-2 rounded-xl border px-4 py-3 font-semibold ${secondaryButtonClass}`}
                  >
                    <FileUp size={18} />
                    Choose PDF Resume
                  </button>

                  {selectedFile && (
                    <p className={`text-sm ${mutedClass}`}>
                      Selected: {selectedFile.name} ({formatFileSize(selectedFile.size)})
                    </p>
                  )}

                  <button
                    type="button"
                    onClick={handleUpload}
                    disabled={!selectedFile || uploading}
                    className="flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 px-4 py-3 font-semibold text-white disabled:opacity-50"
                  >
                    <Upload size={18} />
                    {uploading ? 'Uploading...' : 'Upload Resume'}
                  </button>
                </div>
              </div>

              <div
                className={`rounded-2xl border p-6 ${darkMode
                    ? 'border-indigo-900 bg-gradient-to-br from-indigo-950 via-slate-900 to-violet-950'
                    : 'border-indigo-100 bg-gradient-to-br from-indigo-50 via-white to-violet-50'
                  }`}
              >
                <div
                  className={`mb-4 inline-flex rounded-xl p-3 text-indigo-500 shadow-sm ${darkMode ? 'bg-slate-900' : 'bg-white'
                    }`}
                >
                  <Sparkles size={22} />
                </div>
                <h2 className="text-xl font-bold">
                  Your next opportunity starts here
                </h2>
                <p className={`mt-3 text-sm leading-6 ${mutedClass}`}>
                  Keep an up-to-date resume ready for job applications. Your
                  uploaded files will appear in your resume library.
                </p>
                <div className={`mt-6 space-y-3 text-sm ${darkMode ? 'text-slate-300' : 'text-slate-700'}`}>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 size={17} className="text-emerald-500" />
                    Upload PDF resumes
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 size={17} className="text-emerald-500" />
                    View upload status and file details
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 size={17} className="text-emerald-500" />
                    Keep resume lists separated by account
                  </div>
                </div>
              </div>
            </section>
          </div>
        )}
        {activeTab === 'my-resumes' && (
  <section className="space-y-6">

    {/* Resume library heading */}
    <div className="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 className="text-xl font-bold">Your Resume Library</h2>
        <p className={`mt-1 text-sm ${mutedClass}`}>
          All your uploaded resumes, organized in one place.
        </p>
      </div>

      <button
        type="button"
        onClick={() => setActiveTab('upload')}
        className="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-indigo-500"
      >
        <Upload size={16} />
        Add New Resume
      </button>
    </div>

    {/* Loading state */}
    {loading ? (
      <div className={`rounded-2xl border p-12 text-center ${cardClass}`}>
        <RefreshCw size={28} className="mx-auto animate-spin text-indigo-500" />
        <p className={`mt-4 text-sm ${mutedClass}`}>
          Loading your resumes...
        </p>
      </div>
    ) : resumes.length === 0 ? (
      /* Empty state */
      <div className={`rounded-2xl border p-12 text-center ${cardClass}`}>
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-indigo-500/10 text-indigo-500">
          <FileText size={32} />
        </div>

        <h3 className="mt-5 text-lg font-bold">Your resume library is empty</h3>

        <p className={`mx-auto mt-2 max-w-md text-sm ${mutedClass}`}>
          Upload your first resume to start organizing your professional documents.
        </p>

        <button
          type="button"
          onClick={() => setActiveTab('upload')}
          className="mt-6 rounded-xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white hover:bg-indigo-500"
        >
          Upload Your First Resume
        </button>
      </div>
    ) : (
      <>
        {/* Resume count */}
        <div className={`flex items-center gap-2 text-sm ${mutedClass}`}>
          <FileText size={16} />
          Showing {resumes.length} {resumes.length === 1 ? 'resume' : 'resumes'}
        </div>

        {/* Resume cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
{resumes.map((resume) => (
  <ResumeCard
    key={resume.id}
    resume={resume}
    darkMode={darkMode}
    mutedClass={mutedClass}
    onView={handleViewResume}
    onDelete={handleDeleteResume}
    isLoading={previewLoadingId === resume.id}
    isDeleting={deletingResumeId === resume.id}
  />
))}
        </div>
      </>
    )}
  </section>
)}
        {activeTab === 'improve' && (
  <section className={`rounded-2xl border p-6 ${cardClass}`}>
    <div className="mb-4 inline-flex rounded-xl bg-indigo-500/10 p-3 text-indigo-500">
      <Sparkles size={24} />
    </div>

    <h2 className="text-xl font-bold">Improve Your Resume</h2>

    <p className={`mt-2 text-sm ${mutedClass}`}>
      Get AI-powered feedback on your professional summary, skills,
      strengths and opportunities to improve your resume.
    </p>

    {resumes.length === 0 ? (
      <p className={`mt-5 text-sm ${mutedClass}`}>
        Upload a resume first to get started.
      </p>
    ) : (
      <div className="mt-5 flex flex-col gap-3 sm:flex-row">
        <select
          value={selectedResumeId}
          onChange={(event) => {
            setSelectedResumeId(event.target.value);
            setAnalysis(null);
            setError('');
            setNotice('');
          }}
          disabled={analyzing}
          className={`w-full max-w-md rounded-xl border p-3 ${secondaryButtonClass}`}
        >
          <option value="">Select a resume</option>

          {resumes.map((resume) => (
            <option key={resume.id} value={resume.id}>
              {resume.originalFilename}
            </option>
          ))}
        </select>

        <button
          type="button"
          onClick={handleAnalyzeResume}
          disabled={!selectedResumeId || analyzing}
          className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <Sparkles size={17} />
          {analyzing ? 'Analyzing...' : 'Analyze Resume'}
        </button>
      </div>
    )}

    {analyzing && (
  <div className={`mt-6 rounded-xl border p-5 ${subtlePanelClass}`}>
    <div className="flex items-center gap-3">
      <RefreshCw className="animate-spin text-indigo-500" size={20} />

      <div>
        <p className="font-semibold">
          {analysisStatus === 'PENDING'
            ? 'Analysis queued'
            : analysisStatus === 'PROCESSING'
              ? 'AI is analyzing your resume'
              : 'Starting resume analysis'}
        </p>

        <p className={`mt-1 text-sm ${mutedClass}`}>
          {analysisStatus === 'PENDING'
            ? 'Your resume is waiting to be processed.'
            : 'AI is extracting insights from your resume. This may take a little time.'}
        </p>
      </div>
    </div>
  </div>
)}

    {analysis && (
      <div className="mt-8 space-y-5">
        <div>
          <h3 className="text-lg font-bold">
            Analysis Results
          </h3>
          <p className={`mt-1 text-sm ${mutedClass}`}>
  {resumes.find(
    (resume) => String(resume.id) === String(selectedResumeId)
  )?.originalFilename || 'Resume analysis'}
</p>
        </div>

        <div className={`rounded-xl border p-5 ${subtlePanelClass}`}>
          <h4 className="font-semibold">Professional Summary</h4>
          <p className={`mt-3 text-sm leading-7 ${mutedClass}`}>
            {analysis.professionalSummary || 'No summary identified.'}
          </p>
        </div>

        {[
          {
            title: 'Technical Skills',
            items: analysis.technicalSkills,
          },
          {
            title: 'Soft Skills',
            items: analysis.softSkills,
          },
          {
            title: 'Professional Strengths',
            items: analysis.strengths,
          },
          {
            title: 'Recommendations',
            items: analysis.recommendations,
          },
        ].map((section) => (
          <div
            key={section.title}
            className={`rounded-xl border p-5 ${subtlePanelClass}`}
          >
            <h4 className="font-semibold">{section.title}</h4>

            {Array.isArray(section.items) && section.items.length > 0 ? (
              <ul className={`mt-3 space-y-2 text-sm ${mutedClass}`}>
                {section.items.map((item, index) => (
                  <li key={index} className="flex gap-2">
                    <span className="text-indigo-500">•</span>
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className={`mt-3 text-sm ${mutedClass}`}>
                No information identified.
              </p>
            )}
          </div>
        ))}
      </div>
    )}
  </section>
)}

        <footer className={`mt-6 flex flex-col gap-2 text-xs sm:flex-row sm:items-center sm:justify-between ${mutedClass}`}>
          <span>AI Job Copilot · Resume Manager</span>
          <span>Resume data is loaded from your authenticated account.</span>
        </footer>
        {previewUrl && (
  <div
  className="fixed inset-0 z-[100] flex items-center justify-center bg-black/80 p-3 sm:p-6"
  onClick={() => setPreviewUrl(null)}
  >
    <div
      className={`flex h-[90vh] min-h-0 w-[200vw] max-w-6xl flex-col overflow-hidden rounded-2xl border shadow-2xl ${
        darkMode
          ? 'border-slate-700 bg-slate-900'
          : 'border-slate-200 bg-white'
      }`}
      onClick={(event) => event.stopPropagation()}
    >
      <div className="flex items-center justify-between gap-3 border-b border-slate-700 px-4 py-3">
        <div className="min-w-0">
          <h2 className="truncate text-sm font-bold">
            {previewFilename}
          </h2>
          <p className="text-xs text-slate-400">Resume Preview</p>
        </div>

        <button
          type="button"
          onClick={() => setPreviewUrl(null)}
          className="flex shrink-0 items-center gap-2 rounded-lg bg-red-500/10 px-3 py-2 text-sm font-semibold text-red-500 hover:bg-red-500/20"
        >
          <X size={17} />
          Close
        </button>
      </div>

      <div className="min-h-0 flex-1 bg-slate-200">
        <iframe
          src={previewUrl}
          title="Resume PDF Preview"
          className="h-full w-full"
        />
      </div>
    </div>
  </div>
)}
      </div>
    </main>
  );
}

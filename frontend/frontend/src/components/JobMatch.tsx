
import { useEffect, useState } from 'react';

type JobMatchProps = {
  token: string;
  theme: 'dark' | 'light';
};

type Resume = {
  id: number;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  status: string;
  createdAt: string;
  updatedAt: string;
};

type MatchResult = {
  resumeId: number;
  filename: string;
  matchScore: number;
  matchingSkills: string[];
  missingSkills: string[];
  matchedRequirements: string[];
  experienceGaps: string[];
  recommendations: string[];
};

const API_BASE_URL = 'http://localhost:8082/api/resumes';

export default function JobMatch({ token, theme }: JobMatchProps) {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [selectedResume, setSelectedResume] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [result, setResult] = useState<MatchResult | null>(null);

  const [loadingResumes, setLoadingResumes] = useState(true);
  const [matching, setMatching] = useState(false);
  const [error, setError] = useState('');

  const isDark = theme === 'dark';

  const cardClass = isDark
    ? 'bg-gray-800 border-gray-700 text-white'
    : 'bg-white border-gray-200 text-gray-900';

  const inputClass = isDark
    ? 'bg-gray-900 border-gray-600 text-white placeholder-gray-400'
    : 'bg-white border-gray-300 text-gray-900 placeholder-gray-500';

  // Fetch uploaded resumes
  useEffect(() => {
    const fetchResumes = async () => {
      try {
        const response = await fetch(API_BASE_URL, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          throw new Error('Failed to fetch resumes.');
        }

        const data: Resume[] = await response.json();

        setResumes(data);

        if (data.length > 0) {
          setSelectedResume(String(data[0].id));
        }
      } catch (err) {
        setError(
          err instanceof Error ? err.message : 'Something went wrong.'
        );
      } finally {
        setLoadingResumes(false);
      }
    };

    if (token) {
      fetchResumes();
    } else {
      setError('Please log in to use AI Job Match.');
      setLoadingResumes(false);
    }
  }, [token]);

  // Match resume with job description
  const handleMatch = async () => {
    if (!selectedResume) {
      setError('Please select a resume.');
      return;
    }

    if (!jobDescription.trim()) {
      setError('Please enter a job description.');
      return;
    }

    setMatching(true);
    setError('');
    setResult(null);

    try {
      const response = await fetch(
        `${API_BASE_URL}/${selectedResume}/match`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            jobDescription: jobDescription.trim(),
          }),
        }
      );

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || 'Failed to analyze job match.');
      }

      const data: MatchResult = await response.json();
      setResult(data);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Unable to analyze job match.'
      );
    } finally {
      setMatching(false);
    }
  };

  const renderList = (title: string, items: string[], color: string) => (
    <div>
      <h3 className="font-semibold mb-3">{title}</h3>

      {items.length > 0 ? (
        <ul className="space-y-2">
          {items.map((item, index) => (
            <li key={`${item}-${index}`} className="flex gap-2 text-sm">
              <span className={color}>•</span>
              <span>{item}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-sm opacity-60">No items identified.</p>
      )}
    </div>
  );

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold">AI Job Match</h1>
        <p className="text-sm opacity-70 mt-2">
          Compare your resume with a job description and identify skill gaps.
        </p>
      </div>

      {/* Input section */}
      <div className={`border rounded-xl p-6 space-y-5 ${cardClass}`}>
        <div>
          <label className="block text-sm font-medium mb-2">
            Select Resume
          </label>

          {loadingResumes ? (
            <p className="text-sm opacity-70">Loading resumes...</p>
          ) : resumes.length === 0 ? (
            <p className="text-sm text-red-500">
              No resumes found. Please upload a resume first.
            </p>
          ) : (
            <select
              value={selectedResume}
              onChange={(e) => {
                setSelectedResume(e.target.value);
                setResult(null);
              }}
              className={`w-full border rounded-lg p-3 outline-none ${inputClass}`}
            >
              {resumes.map((resume) => (
                <option key={resume.id} value={resume.id}>
                  {resume.originalFilename}
                </option>
              ))}
            </select>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">
            Job Description
          </label>

          <textarea
            value={jobDescription}
            onChange={(e) => setJobDescription(e.target.value)}
            placeholder="Paste the complete job description here..."
            rows={9}
            maxLength={15000}
            className={`w-full border rounded-lg p-3 outline-none resize-y ${inputClass}`}
          />

          <p className="text-xs opacity-60 text-right mt-1">
            {jobDescription.length}/15000 characters
          </p>
        </div>

        {error && (
          <p className="text-sm text-red-500">{error}</p>
        )}

        <button
          onClick={handleMatch}
          disabled={
            matching ||
            loadingResumes ||
            resumes.length === 0 ||
            !selectedResume ||
            !jobDescription.trim()
          }
          className="px-6 py-3 rounded-lg bg-blue-600 hover:bg-blue-700
                     text-white font-medium disabled:opacity-50
                     disabled:cursor-not-allowed"
        >
          {matching ? 'Analyzing...' : 'Analyze Job Match'}
        </button>
      </div>

      {/* Results section */}
      {result && (
        <div className={`border rounded-xl p-6 space-y-6 ${cardClass}`}>
          <div>
            <h2 className="text-xl font-bold">Match Analysis</h2>
            <p className="text-sm opacity-70 mt-1">
              Resume: {result.filename}
            </p>
          </div>

          {/* Match score */}
          <div className="text-center py-5">
            <p className="text-sm opacity-70 mb-2">
              Estimated Resume Match
            </p>

            <p className="text-5xl font-bold text-blue-500">
              {result.matchScore}%
            </p>

            <div className="w-full max-w-md mx-auto mt-4 h-3 rounded-full bg-gray-300 overflow-hidden">
              <div
                className="h-full bg-blue-600 rounded-full transition-all"
                style={{
                  width: `${Math.min(
                    100,
                    Math.max(0, result.matchScore)
                  )}%`,
                }}
              />
            </div>

            <p className="text-xs opacity-60 mt-3">
              AI-estimated alignment, not an ATS score or hiring probability.
            </p>
          </div>

          <hr className="border-gray-500/30" />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {renderList(
              'Matching Skills',
              result.matchingSkills,
              'text-green-500'
            )}

            {renderList(
              'Missing Skills',
              result.missingSkills,
              'text-red-500'
            )}

            {renderList(
              'Matched Requirements',
              result.matchedRequirements,
              'text-green-500'
            )}

            {renderList(
              'Experience Gaps',
              result.experienceGaps,
              'text-yellow-500'
            )}
          </div>

          <hr className="border-gray-500/30" />

          {renderList(
            'Recommendations',
            result.recommendations,
            'text-blue-500'
          )}
        </div>
      )}
    </div>
  );
}

import { useState } from 'react';
import ResumeApp from './ResumeApp.jsx';
import './index.css';

function App() {
  const [theme, setTheme] = useState('dark');

  return (
    <div className={theme === 'dark' ? 'dark' : ''}>
      <ResumeApp token={localStorage.getItem('accessToken') || localStorage.getItem('token') || sessionStorage.getItem('accessToken') || sessionStorage.getItem('token') || ''} theme={theme} setTheme={setTheme} />
    </div>
  );
}

export default App;
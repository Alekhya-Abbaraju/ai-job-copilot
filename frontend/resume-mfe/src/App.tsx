import { useState } from 'react';
import ResumeApp from './ResumeApp.jsx';
import './index.css';

function App() {
  const [theme, setTheme] = useState('dark');

  return (
    <div className={theme === 'dark' ? 'dark' : ''}>
      <ResumeApp theme={theme} setTheme={setTheme} />
    </div>
  );
}

export default App;
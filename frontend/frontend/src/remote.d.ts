declare module 'jobs_mfe/JobsApp' {
  import type { ComponentType } from 'react';

  type Theme = 'dark' | 'light';

  const JobsApp: ComponentType<{
    theme: Theme;
    setTheme: (theme: Theme) => void;
  }>;

  export default JobsApp;
}

declare module 'resume_mfe/ResumeApp' {
  import type { ComponentType } from 'react';

  type Theme = 'dark' | 'light';

  const ResumeApp: ComponentType<{
    token?: string;
    theme: Theme;
    setTheme: (theme: Theme) => void;
  }>;

  export default ResumeApp;
}

declare module 'auth_mfe/AuthContainer' {
  import type { ComponentType } from 'react'

  const AuthContainer: ComponentType<{
    onLoginSuccess?: (userData: {
      name?: string
      email?: string
      [key: string]: unknown
    }) => void
  }>

  export default AuthContainer
}
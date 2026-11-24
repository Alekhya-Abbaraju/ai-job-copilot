
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { federation } from '@module-federation/vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')

  return {
    plugins: [
      react(),
      tailwindcss(),
      federation({
        name: 'job_copilot_host',
        dts: false,
        remotes: {
          jobs_mfe: {
            type: 'module',
            name: 'jobs_mfe',
            entry: env.VITE_JOBS_MFE_URL || 'http://localhost:5174/remoteEntry.js',
            entryGlobalName: 'jobs_mfe',
            shareScope: 'default',
          },
          auth_mfe: {
            type: 'module',
            name: 'auth_mfe',
            entry: env.VITE_AUTH_MFE_URL || 'http://localhost:5175/remoteEntry.js',
            entryGlobalName: 'auth_mfe',
            shareScope: 'default',
          },
          resume_mfe: {
            type: 'module',
            name: 'resume_mfe',
            entry: env.VITE_RESUME_MFE_URL || 'http://localhost:5176/remoteEntry.js',
            entryGlobalName: 'resume_mfe',
            shareScope: 'default',
          },
        },
        shared: ['react', 'react-dom', 'lucide-react'],
      }),
    ],
    server: {
      port: 5143,
      strictPort: true,
    },
    build: {
      target: 'esnext',
    },
  }
})

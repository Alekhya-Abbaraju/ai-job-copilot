
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { federation } from '@module-federation/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    federation({
      name: 'jobs_mfe',
      dts: false,
      filename: 'remoteEntry.js',
      exposes: {
        './JobsApp': './src/JobsApp.jsx',
      },
      shared: ['react', 'react-dom', 'lucide-react'], // Added lucide-react here
    }),
  ],

  server: {
    port: 5174,
    strictPort: true,
  },
  preview: {
    port: 4174,
    strictPort: true,
  },
  build: {
    target: 'esnext',
  },
})
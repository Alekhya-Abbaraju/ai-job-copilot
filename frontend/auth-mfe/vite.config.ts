import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { federation } from '@module-federation/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    federation({
      name: 'auth_mfe',
      filename: 'remoteEntry.js',
      exposes: {
  './AuthContainer': './src/Components/authContainer.jsx',
},
      shared: {
        react: {
          singleton: true,
          requiredVersion: '^18.0.0',
        },
        'react-dom': {
          singleton: true,
          requiredVersion: '^18.0.0',
        },
      },
    }),
  ],
  server: {
    port: 5175,
    strictPort: true,
  },
  build: {
    target: 'esnext',
    minify: false,
  },
})
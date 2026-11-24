
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { federation } from '@module-federation/vite';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    federation({
      name: 'resume_mfe',
      dts: false,
      filename: 'remoteEntry.js',
      exposes: {
        './ResumeApp': './src/ResumeApp.jsx',
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
    port: 5176,
    strictPort: true,
  },
  build: {
    target: 'esnext',
    minify: false,
  },
});
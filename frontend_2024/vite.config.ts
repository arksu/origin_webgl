import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue()
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3070,
    proxy: {
      '/api': 'http://0.0.0.0:8110',
      '/api/game': {
        target: 'ws://0.0.0.0:8110',
        ws: true
      }
    }
  }
})

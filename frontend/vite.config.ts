import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue()
  ],
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler'
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    assetsDir: 'js',
    rollupOptions: {
      output: {
        // manualChunks: (id) => {
        //   if (id) {
        //     const p = id.split('node_modules/')[1]
        //     if (p) {
        //       if (p.includes('vue')) {
        //         return 'vue'
        //       } else if (p.includes('pixi.js/lib/scene')) {
        //         return 'pixi-0'
        //       } else if (p.includes('pixi.js/lib/rendering')) {
        //         return 'pixi-1'
        //       } else if (p.includes('@pixi')) {
        //         return 'pixi-2'
        //       } else if (p.includes('pixi.js')) {
        //         return 'pixi'
        //       } else {
        //         return 'ext'
        //       }
        //     }
        //   }
        //
        //   return 'vendor'
        // }
      }
    }
  },
  server:
    {
      host: '0.0.0.0',
      port: 3070,
      proxy:
        {
          '/api': 'http://0.0.0.0:8010',
          '/api/game':
            {
              target: 'ws://0.0.0.0:8010',
              ws: true
            }
        }
    }
})

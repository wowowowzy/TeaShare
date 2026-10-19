import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
// import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    // 部署到 https://wowowzy.art/tea/admin/ 子目录下，必须设置 base，
    // 否则构建产物使用绝对路径 /assets/... 会 404 白屏。
    base: env.VITE_BASE || '/tea/admin/',
    plugins: [
      vue(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      proxy: {
        // 本地开发时把后端接口代理到本机 Spring Boot，避免跨域
        '/api': {
          target: env.VITE_PROXY_TARGET || 'http://127.0.0.1:8888',
          changeOrigin: true
        }
      }
    }
  }
})

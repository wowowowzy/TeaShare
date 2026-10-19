import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    // 部署到 https://wowowzy.art/tea/ 子目录下，必须设置 base，
    // 否则构建产物使用绝对路径 /assets/... 会 404 白屏。
    base: env.VITE_BASE || '/tea/',
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
        // 3D 模型走 OSS（保持原有行为，需放在 /api 之前，Vite 按声明顺序匹配）
        '/api/model': {
          target: 'https://java-web-itheima-zy.oss-cn-beijing.aliyuncs.com',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api\/model/, '')
        },
        // 本地开发时把后端接口代理到本机 Spring Boot，避免跨域
        '/api': {
          target: env.VITE_PROXY_TARGET || 'http://127.0.0.1:8888',
          changeOrigin: true
        }
      }
    }
  }
})

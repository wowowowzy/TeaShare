import { fileURLToPath, URL } from 'node:url'
import fs from 'node:fs'
import path from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

/**
 * 生产构建剔除 40MB 的本地 3D 模型副本。
 *
 * `public/tea.glb` 仅供本地开发使用（见 TraditionView 的 GLB_MODEL_URL 开关）：
 * 开发环境同源加载，生产环境走 OSS。Vite 会把 publicDir 全量复制到 outDir，
 * 因此需要在构建结束后删除，避免产物里躺着 40MB 永不被引用的死重量。
 */
function dropDevOnlyModel() {
  let outDir = 'dist'
  return {
    name: 'drop-dev-only-model',
    apply: 'build',
    configResolved(config) {
      outDir = config.build.outDir
    },
    closeBundle() {
      // 注意：publicDir 的内容被复制到 outDir 的「根」，而不是 outDir/<base>/。
      const target = path.resolve(process.cwd(), outDir, 'tea.glb')
      if (fs.existsSync(target)) {
        const mb = (fs.statSync(target).size / 1024 / 1024).toFixed(1)
        fs.rmSync(target)
        console.log(
          `\n[drop-dev-only-model] 已从生产产物移除 ${mb}MB 开发专用模型：${path.relative(process.cwd(), target)}`
        )
      }
    }
  }
}

// 部署约定：用户端挂在 https://wowowzy.art/tea/ 子目录下。
// base 必须是 '/tea/'，否则构建产物使用绝对路径 /assets/... 会 404 白屏。
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    base: env.VITE_BASE || '/tea/',
    plugins: [vue(), tailwindcss(), dropDevOnlyModel()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      proxy: {
        // 本地开发把后端接口代理到本机 Spring Boot，避免跨域，
        // 同时让验证码依赖的 Session Cookie 成为第一方 Cookie。
        '/api': {
          target: env.VITE_PROXY_TARGET || 'http://127.0.0.1:8888',
          changeOrigin: true
        }
      }
    }
  }
})

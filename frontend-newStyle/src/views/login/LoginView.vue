<template>
  <div class="min-h-screen pt-20 md:pt-28">
    <div class="container-editorial py-16 md:py-24">
      <div class="grid grid-cols-1 gap-16 md:grid-cols-12 md:gap-24">
        <!-- 左：标题 -->
        <div class="md:col-span-6">
          <SectionLabel class="mb-6 block">欢迎回来</SectionLabel>
          <h1 class="font-serif text-5xl leading-[0.95] tracking-tighter md:text-7xl">
            {{ isLogin ? '登录' : '注册' }}
            <span class="block text-[#1C1C1C]/40 italic">茗承古韵</span>
          </h1>
          <p class="mt-8 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
            {{
              isLogin
                ? '登录后可使用 AI 小茗、茶友社、购物车与订单管理。'
                : '注册一个账号，开始记录属于你的茶路。'
            }}
          </p>

          <div class="mt-12 border-t border-[#1C1C1C]/10 pt-8">
            <p class="font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">茶语</p>
            <blockquote class="mt-4 max-w-md font-serif text-xl leading-relaxed tracking-tight italic">
              「一碗喉吻润，二碗破孤闷，三碗搜枯肠。」
            </blockquote>
          </div>
        </div>

        <!-- 右：表单 -->
        <div class="md:col-span-6">
          <form class="space-y-8" novalidate @submit.prevent="handleSubmit">
            <!-- 用户名 -->
            <div class="relative">
              <input
                id="username"
                v-model.trim="form.username"
                type="text"
                class="field-input peer font-serif text-xl"
                placeholder=" "
                autocomplete="username"
                @blur="validateField('username')"
              />
              <label
                for="username"
                class="pointer-events-none absolute top-2 left-0 font-sans text-sm text-[#1C1C1C]/40 transition-all duration-300 peer-focus:top-0 peer-focus:text-xs peer-focus:tracking-[0.15em] peer-focus:uppercase peer-[:not(:placeholder-shown)]:top-0 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:tracking-[0.15em] peer-[:not(:placeholder-shown)]:uppercase"
              >
                用户名
              </label>
              <p v-if="errors.username" class="mt-2 font-sans text-xs text-[#1C1C1C]/60">
                {{ errors.username }}
              </p>
            </div>

            <!-- 密码 -->
            <div class="relative">
              <input
                id="password"
                v-model="form.password"
                type="password"
                class="field-input peer font-serif text-xl"
                placeholder=" "
                :autocomplete="isLogin ? 'current-password' : 'new-password'"
                @blur="validateField('password')"
              />
              <label
                for="password"
                class="pointer-events-none absolute top-2 left-0 font-sans text-sm text-[#1C1C1C]/40 transition-all duration-300 peer-focus:top-0 peer-focus:text-xs peer-focus:tracking-[0.15em] peer-focus:uppercase peer-[:not(:placeholder-shown)]:top-0 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:tracking-[0.15em] peer-[:not(:placeholder-shown)]:uppercase"
              >
                密码
              </label>
              <p v-if="errors.password" class="mt-2 font-sans text-xs text-[#1C1C1C]/60">
                {{ errors.password }}
              </p>
            </div>

            <!-- 注册：确认密码 + 手机号 -->
            <template v-if="!isLogin">
              <div class="relative">
                <input
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  type="password"
                  class="field-input peer font-serif text-xl"
                  placeholder=" "
                  autocomplete="new-password"
                  @blur="validateField('confirmPassword')"
                />
                <label
                  for="confirmPassword"
                  class="pointer-events-none absolute top-2 left-0 font-sans text-sm text-[#1C1C1C]/40 transition-all duration-300 peer-focus:top-0 peer-focus:text-xs peer-focus:tracking-[0.15em] peer-focus:uppercase peer-[:not(:placeholder-shown)]:top-0 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:tracking-[0.15em] peer-[:not(:placeholder-shown)]:uppercase"
                >
                  确认密码
                </label>
                <p v-if="errors.confirmPassword" class="mt-2 font-sans text-xs text-[#1C1C1C]/60">
                  {{ errors.confirmPassword }}
                </p>
              </div>

              <div class="relative">
                <input
                  id="phone"
                  v-model.trim="form.phone"
                  type="text"
                  class="field-input peer font-serif text-xl"
                  placeholder=" "
                  autocomplete="tel"
                  @blur="validateField('phone')"
                />
                <label
                  for="phone"
                  class="pointer-events-none absolute top-2 left-0 font-sans text-sm text-[#1C1C1C]/40 transition-all duration-300 peer-focus:top-0 peer-focus:text-xs peer-focus:tracking-[0.15em] peer-focus:uppercase peer-[:not(:placeholder-shown)]:top-0 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:tracking-[0.15em] peer-[:not(:placeholder-shown)]:uppercase"
                >
                  手机号
                </label>
                <p v-if="errors.phone" class="mt-2 font-sans text-xs text-[#1C1C1C]/60">
                  {{ errors.phone }}
                </p>
              </div>
            </template>

            <!-- 登录：验证码 -->
            <div v-else>
              <div class="flex items-end gap-6">
                <div class="relative flex-1">
                  <input
                    id="captcha"
                    v-model.trim="form.captcha"
                    type="text"
                    class="field-input peer font-serif text-xl"
                    placeholder=" "
                    maxlength="6"
                    autocomplete="off"
                    @blur="validateField('captcha')"
                  />
                  <label
                    for="captcha"
                    class="pointer-events-none absolute top-2 left-0 font-sans text-sm text-[#1C1C1C]/40 transition-all duration-300 peer-focus:top-0 peer-focus:text-xs peer-focus:tracking-[0.15em] peer-focus:uppercase peer-[:not(:placeholder-shown)]:top-0 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:tracking-[0.15em] peer-[:not(:placeholder-shown)]:uppercase"
                  >
                    验证码
                  </label>
                </div>
                <button
                  type="button"
                  class="h-12 w-32 shrink-0 border border-[#1C1C1C]/15 transition-colors duration-300 hover:border-[#1C1C1C]"
                  aria-label="点击刷新验证码"
                  @click="refreshCaptcha"
                >
                  <img
                    v-if="captchaUrl"
                    :src="captchaUrl"
                    alt="验证码"
                    class="h-full w-full object-contain"
                  />
                  <span v-else class="font-sans text-xs text-[#1C1C1C]/40">加载中</span>
                </button>
              </div>
              <p v-if="errors.captcha" class="mt-2 font-sans text-xs text-[#1C1C1C]/60">
                {{ errors.captcha }}
              </p>
            </div>

            <button type="submit" class="btn-editorial w-full" :disabled="submitting">
              {{ submitting ? '处理中…' : isLogin ? '登录' : '注册' }}
            </button>

            <p class="text-center font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40">
              {{ isLogin ? '还没有账号？' : '已有账号？' }}
              <button
                type="button"
                class="link-underline text-[#1C1C1C]"
                @click="toggleMode"
              >
                {{ isLogin ? '立即注册' : '返回登录' }}
              </button>
            </p>

            <p class="text-center font-sans text-xs leading-relaxed text-[#1C1C1C]/30">
              登录即表示同意本平台的服务条款与隐私政策，AI 生成内容仅供参考。
            </p>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import { fetchCaptchaImage, verifyCaptcha } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const submitting = ref(false)
const captchaUrl = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  captcha: ''
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  captcha: ''
})

const USERNAME_RE = /^[\w\u4e00-\u9fa5]{2,10}$/
const PASSWORD_RE = /^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?~`]{6,20}$/
const PHONE_RE = /^1[3-9]\d{9}$/

function validateField(field) {
  const v = form[field]
  switch (field) {
    case 'username':
      errors.username = !v
        ? '请输入用户名'
        : USERNAME_RE.test(v)
          ? ''
          : '长度 2-10 位，只能包含字母、数字、下划线和中文'
      break
    case 'password':
      errors.password = !v
        ? '请输入密码'
        : PASSWORD_RE.test(v)
          ? ''
          : '长度 6-20 位，只能包含字母、数字和特殊符号'
      break
    case 'confirmPassword':
      errors.confirmPassword = v !== form.password ? '两次密码输入不一致' : ''
      break
    case 'phone':
      errors.phone = !v ? '请输入手机号' : PHONE_RE.test(v) ? '' : '手机号格式不正确'
      break
    case 'captcha':
      errors.captcha = !v ? '验证码不能为空' : ''
      break
  }
  return !errors[field]
}

function validateForm() {
  const fields = isLogin.value
    ? ['username', 'password', 'captcha']
    : ['username', 'password', 'confirmPassword', 'phone']
  return fields.map(validateField).every(Boolean)
}

async function refreshCaptcha() {
  try {
    const blob = await fetchCaptchaImage()
    if (captchaUrl.value) URL.revokeObjectURL(captchaUrl.value)
    captchaUrl.value = URL.createObjectURL(blob)
  } catch {
    ElMessage.error('验证码加载失败，请稍后重试')
  }
}

function toggleMode() {
  isLogin.value = !isLogin.value
  Object.keys(errors).forEach((k) => (errors[k] = ''))
  form.captcha = ''
  form.confirmPassword = ''
  if (isLogin.value) refreshCaptcha()
}

async function handleSubmit() {
  if (!validateForm()) return
  submitting.value = true
  try {
    if (isLogin.value) {
      // 先校验验证码（与图片接口共用同一 Session Cookie）
      const check = await verifyCaptcha(form.captcha)
      if (check.code !== 1 && check.code !== 200) {
        ElMessage.error(check.msg || '验证码错误')
        form.captcha = ''
        await refreshCaptcha()
        return
      }
      const ok = await userStore.login({ username: form.username, password: form.password })
      if (ok) {
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error('登录失败，请检查用户名和密码')
        await refreshCaptcha()
      }
    } else {
      await userStore.register({
        username: form.username,
        password: form.password,
        phone: form.phone
      })
      isLogin.value = true
      form.password = ''
      form.confirmPassword = ''
      await refreshCaptcha()
    }
  } catch {
    /* 拦截器已提示错误 */
  } finally {
    submitting.value = false
  }
}

// 登录模式默认加载验证码
refreshCaptcha()

onUnmounted(() => {
  if (captchaUrl.value) URL.revokeObjectURL(captchaUrl.value)
})
</script>

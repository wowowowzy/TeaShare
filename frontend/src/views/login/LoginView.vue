<template>
  <div class="login">
    <!-- 导航栏组件 -->
    <Navbar />
    <div class="login-container">
      <h1 class="login-title">{{ isLogin ? '登录' : '注册' }}</h1>
      <form class="login-form" @submit.prevent="handleSubmit">
        <!-- 用户名 -->
        <div class="form-group">
          <label for="username">用户名</label>
          <div class="input-wrapper" :class="{ 'error': errors.username }">
            <input 
              type="text" 
              id="username" 
              v-model="form.username" 
              @blur="validateUsername"
              @input="clearError('username')"
              placeholder="请输入用户名" 
            />
          </div>
          <span class="error-message" v-if="errors.username">{{ errors.username }}</span>
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label for="password">密码</label>
          <div class="input-wrapper" :class="{ 'error': errors.password }">
            <input 
              type="password" 
              id="password" 
              v-model="form.password" 
              @blur="validatePassword"
              @input="clearError('password')"
              placeholder="请输入密码" 
            />
          </div>
          <span class="error-message" v-if="errors.password">{{ errors.password }}</span>
        </div>

        <!-- 确认密码（仅注册） -->
        <div class="form-group" v-if="!isLogin">
          <label for="confirmPassword">确认密码</label>
          <div class="input-wrapper" :class="{ 'error': errors.confirmPassword }">
            <input 
              type="password" 
              id="confirmPassword" 
              v-model="form.confirmPassword" 
              @blur="validateConfirmPassword"
              @input="clearError('confirmPassword')"
              placeholder="请再次输入密码" 
            />
          </div>
          <span class="error-message" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</span>
        </div>

        <!-- 验证码输入框（仅登录） -->
        <div class="form-group" v-if="isLogin">
          <label for="captcha">验证码</label>
          <div class="captcha-container">
            <div class="input-wrapper captcha-input" :class="{ 'error': errors.captcha }">
              <input 
                type="text" 
                id="captcha" 
                v-model="form.captcha" 
                @blur="validateCaptcha"
                @input="clearError('captcha')"
                placeholder="请输入验证码" 
              />
            </div>
            <div class="captcha-image">
              <img :src="captchaUrl" @click="refreshCaptcha" alt="验证码" />
            </div>
          </div>
          <span class="error-message" v-if="errors.captcha">{{ errors.captcha }}</span>
        </div>

        <!-- 手机号（仅注册） -->
        <div class="form-group" v-if="!isLogin">
          <label for="phone">手机号</label>
          <div class="input-wrapper" :class="{ 'error': errors.phone }">
            <input 
              type="tel" 
              id="phone" 
              v-model="form.phone" 
              @blur="validatePhone"
              @input="clearError('phone')"
              placeholder="请输入手机号" 
            />
          </div>
          <span class="error-message" v-if="errors.phone">{{ errors.phone }}</span>
        </div>

        <!-- 提交按钮 -->
        <button type="submit" class="btn btn-primary">
          {{ isLogin ? '登录' : '注册' }}
        </button>
      </form>

      <div class="switch-mode">
        <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
        <a href="#" @click.prevent="toggleMode">{{ isLogin ? '立即注册' : '立即登录' }}</a>
      </div>
      <div style="text-align: center; font-size: 12px; color: #999;">本网站素材均来源于网络，未进行商用</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import Navbar from '../../components/Navbar.vue';
import { useUserStore } from '../../stores/user';
import { verifyCaptcha } from '../../api/user';
import { API_BASE } from '../../utils/apiBase';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

// 表单数据
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  captcha: ''
});

// 错误信息
const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  captcha: ''
});

// 验证码图片URL
const captchaUrl = ref('');

// 是否为登录模式
const isLogin = ref(true);

// 验证规则
const validators = {
  // 用户名验证：字母、数字、下划线，中文，长度2-10位
  username: (value) => {
    if (!value) return '用户名不能为空';
    if (value.length < 2 || value.length > 10) return '用户名长度需在2-10位之间';
    if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(value)) return '用户名只能包含字母、数字、下划线和中文';
    return '';
  },
  
  // 密码验证：至少包含大小写字母、数字和特殊符号中的一种，长度6-20位
  password: (value) => {
    if (!value) return '密码不能为空';
    if (value.length < 6 || value.length > 20) return '密码长度需在6-20位之间';
    if (!/^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+$/.test(value)) return '密码只能包含字母、数字和特殊符号';
    return '';
  },
  
  // 确认密码验证
  confirmPassword: (value) => {
    if (!value) return '请确认密码';
    if (value !== form.value.password) return '两次输入的密码不一致';
    return '';
  },
  
  // 手机号验证
  phone: (value) => {
    if (!value) return '手机号不能为空';
    if (!/^1[3-9]\d{9}$/.test(value)) return '请输入正确的手机号格式';
    return '';
  },
  
  // 验证码验证
  captcha: (value) => {
    if (!value) return '验证码不能为空';
    return '';
  }
};

// 验证用户名
const validateUsername = () => {
  errors.username = validators.username(form.value.username);
  return !errors.username;
};

// 验证密码
const validatePassword = () => {
  errors.password = validators.password(form.value.password);
  // 如果确认密码已填写，也需要重新验证
  if (form.value.confirmPassword) {
    validateConfirmPassword();
  }
  return !errors.password;
};

// 验证确认密码
const validateConfirmPassword = () => {
  errors.confirmPassword = validators.confirmPassword(form.value.confirmPassword);
  return !errors.confirmPassword;
};

// 验证手机号
const validatePhone = () => {
  errors.phone = validators.phone(form.value.phone);
  return !errors.phone;
};

// 验证验证码
const validateCaptcha = () => {
  errors.captcha = validators.captcha(form.value.captcha);
  return !errors.captcha;
};

// 清除错误信息
const clearError = (field) => {
  errors[field] = '';
};

// 验证整个表单
const validateForm = () => {
  let isValid = true;
  
  isValid = validateUsername() && isValid;
  isValid = validatePassword() && isValid;
  
  if (!isLogin.value) {
    isValid = validateConfirmPassword() && isValid;
    isValid = validatePhone() && isValid;
  } else {
    isValid = validateCaptcha() && isValid;
  }
  
  return isValid;
};

// 切换登录/注册模式
const toggleMode = () => {
  isLogin.value = !isLogin.value;
  // 清空表单
  form.value = {
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
    captcha: ''
  };
  // 清空错误
  Object.keys(errors).forEach(key => {
    errors[key] = '';
  });
  // 如果切换到登录模式，刷新验证码
  if (isLogin.value) {
    refreshCaptcha();
  }
};

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await fetch(`${API_BASE}/api/user/captcha/image`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Cache-Control': 'no-cache'
      }
    });
    
    if (!response.ok) {
      throw new Error('获取验证码失败');
    }
    
    const blob = await response.blob();
    captchaUrl.value = URL.createObjectURL(blob);
  } catch (error) {
    console.error('获取验证码失败:', error);
  }
};

// 提交表单
const handleSubmit = async () => {
  // 表单验证
  if (!validateForm()) {
    ElMessage.error('请检查表单填写是否正确');
    return;
  }
  
  if (isLogin.value) {
    // 登录
    console.log('登录', form.value);
    
    try {
      console.log('验证码值:', form.value.captcha);
      // 第一步：验证验证码
      const captchaRes = await verifyCaptcha(form.value.captcha);
      console.log('验证码验证结果:', captchaRes);

      // 验证码校验失败
      if (captchaRes.code !== 1) {
        ElMessage.error(captchaRes?.message || '验证码错误，请重新输入');
        refreshCaptcha(); // 刷新验证码
        return; // 终止登录流程
      }

      // 第二步：验证码通过后，验证账号密码
      console.log('验证码通过，开始验证账号密码:', form.value.username);
      const loginSuccess = await userStore.login({
        username: form.value.username,
        password: form.value.password
      });

      // 账号密码校验失败
      if (!loginSuccess) {
        ElMessage.error('用户名或密码错误，请重新输入');
        refreshCaptcha(); // 登录失败也刷新验证码，提升安全性
        return;
      }

      // 所有校验通过，跳转首页
      ElMessage.success('登录成功');
      router.push('/');

    } catch (error) {
      console.error('登录校验失败:', error);
      // 区分异常类型给出提示
      if (error.message.includes('验证码')) {
        ElMessage.error('验证码验证失败，请重试');
      } else {
        ElMessage.error('登录请求异常，请稍后再试');
      }
      refreshCaptcha();
    }
  } else {
    // 注册
    const success = await userStore.register({
      username: form.value.username,
      password: form.value.password,
      phone: form.value.phone
    });
    if (success) {
      ElMessage.success('注册成功，请登录');
      isLogin.value = true;
      form.value = {
        username: '',
        password: '',
        confirmPassword: '',
        phone: '',
        captcha: ''
      };
      refreshCaptcha();
    }
  }
};

// 组件挂载时获取验证码
onMounted(() => {
  if (isLogin.value) {
    refreshCaptcha();
  }
});
</script>

<style scoped>
.login {
  min-height: 100vh;
  padding-top: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  background-image: url('https://neeko-copilot.bytedance.net/api/text2image?prompt=广东茶文化背景&size=1920x1080');
  background-size: cover;
  background-position: center;
}

.login-container {
  background-color: rgba(255, 255, 255, 0.9);
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-title {
  font-size: 24px;
  color: #8B4513;
  margin-bottom: 30px;
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: bold;
}

/* 输入框包装器 */
.input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #fff;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #008080;
  box-shadow: 0 0 0 2px rgba(0, 128, 128, 0.1);
}

.input-wrapper.error {
  border-color: #e74c3c;
}

.input-wrapper.error:focus-within {
  box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.1);
}

/* 输入框样式 */
.input-wrapper input {
  width: 100%;
  padding: 10px;
  border: none;
  background: transparent;
  font-size: 16px;
  outline: none;
}

/* 错误提示信息 */
.error-message {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #e74c3c;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 验证码容器样式 */
.captcha-container {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 42px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 提交按钮 */
.btn-primary {
  width: 100%;
  padding: 12px;
  margin-top: 20px;
  background-color: #008080;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-primary:hover {
  background-color: #006666;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 128, 128, 0.3);
}

.btn-primary:active {
  transform: translateY(0);
}

.switch-mode {
  margin-top: 20px;
  text-align: center;
  color: #666;
}

.switch-mode a {
  color: #008080;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 600;
  transition: color 0.3s ease;
}

.switch-mode a:hover {
  color: #006666;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 30px 20px;
    margin: 0 15px;
  }
  
  .captcha-image {
    width: 100px;
    height: 38px;
  }
}
</style>

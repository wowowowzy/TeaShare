<template>
  <div class="login">
    <div class="login-container">
      <h1 class="login-title">管理员登录</h1>
      <form class="login-form" @submit.prevent="handleSubmit">
        <!-- 用户名 -->
        <div class="form-group">
          <label for="username">用户名</label>
          <div class="input-wrapper" :class="{ 'error': errors.username }">
            <input type="text" id="username" v-model="form.username" @blur="validateUsername"
              @input="clearError('username')" placeholder="请输入管理员用户名" />
          </div>
          <span class="error-message" v-if="errors.username">{{ errors.username }}</span>
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label for="password">密码</label>
          <div class="input-wrapper" :class="{ 'error': errors.password }">
            <input type="password" id="password" v-model="form.password" @blur="validatePassword"
              @input="clearError('password')" placeholder="请输入密码" />
          </div>
          <span class="error-message" v-if="errors.password">{{ errors.password }}</span>
        </div>



        <!-- 提交按钮 -->
        <button type="submit" class="btn btn-primary" :disabled="loading">
          <span v-if="loading">
            <i class="loading-icon"></i>
            登录中...
          </span>
          <span v-else>登录</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

// 表单数据
const form = ref({
  username: '',
  password: ''
});

// 错误信息
const errors = reactive({
  username: '',
  password: ''
});

// 加载状态
const loading = ref(false);

// 防抖定时器
let debounceTimer = null;

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


};

// 验证用户名
const validateUsername = () => {
  errors.username = validators.username(form.value.username);
  return !errors.username;
};

// 验证密码
const validatePassword = () => {
  errors.password = validators.password(form.value.password);
  return !errors.password;
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

  return isValid;
};

// 防抖处理函数
const debounce = (func, delay) => {
  return function() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => func.apply(this, arguments), delay);
  };
};

// 提交表单
const handleSubmit = debounce(async () => {
  // 表单验证
  if (!validateForm()) {
    ElMessage.error('请检查表单填写是否正确');
    return;
  }

  // 登录
  loading.value = true;
  try {
    console.log('=== 提交登录表单 ===');
    console.log('用户名:', form.value.username);
    console.log('密码:', form.value.password);
    
    // 调用登录API
    const success = await userStore.login(form.value.username, form.value.password);
    console.log('userStore.login返回值:', success);
    console.log('登录后token:', userStore.token);
    
    if (success) {
      console.log('登录成功，跳转到数据分析大屏');
      ElMessage.success('登录成功');
      // 使用setTimeout确保消息显示后再跳转
      setTimeout(() => {
        router.push('/dashboard');
        console.log('已执行router.push("/dashboard")');
      }, 500);
    } else {
      ElMessage.error('登录失败，请检查用户名和密码');
    }
  } catch (error) {
    // console.error('登录失败:', error);
    // 网络错误或其他错误
    // ElMessage.error('登录失败，请检查网络连接');
  } finally {
    loading.value = false;
    console.log('=== 登录流程结束 ===');
  }
}, 500);


</script>

<style scoped>
.login {
  min-height: 100vh;
  padding-top: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  background-image: url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=中国传统茶文化背景，茶道元素，茶叶，茶壶，古典风格，柔和色调&image_size=landscape_16_9');
  background-size: cover;
  background-position: center;
  position: relative;
}

.login::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.login-container {
  position: relative;
  z-index: 2;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 420px;
  border: 1px solid rgba(139, 69, 19, 0.2);
}

.login-title {
  font-size: 28px;
  color: #8B4513;
  margin-bottom: 30px;
  text-align: center;
  font-family: 'Microsoft YaHei', serif;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  color: #5D4037;
  font-weight: bold;
  font-size: 14px;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 输入框包装器 */
.input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #D7CCC8;
  border-radius: 6px;
  background-color: #fff;
  transition: all 0.3s ease;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.input-wrapper:focus-within {
  border-color: #8B4513;
  box-shadow: 0 0 0 2px rgba(139, 69, 19, 0.15);
}

.input-wrapper.error {
  border-color: #C62828;
}

.input-wrapper.error:focus-within {
  box-shadow: 0 0 0 2px rgba(198, 40, 40, 0.15);
}

/* 输入框样式 */
.input-wrapper input {
  width: 100%;
  padding: 12px 15px;
  border: none;
  background: transparent;
  font-size: 16px;
  outline: none;
  font-family: 'Microsoft YaHei', sans-serif;
}



/* 错误提示信息 */
.error-message {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: #C62828;
  animation: slideIn 0.3s ease;
  font-family: 'Microsoft YaHei', sans-serif;
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

/* 登录按钮加载动画 */
.loading-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 提交按钮 */
.btn-primary {
  width: 100%;
  padding: 14px;
  margin-top: 24px;
  background-color: #8B4513;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Microsoft YaHei', sans-serif;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.3);
}

.btn-primary:hover {
  background-color: #6D4C41;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.4);
}

.btn-primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.3);
}

.btn-primary:disabled {
  background-color: #A1887F;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 30px 20px;
    margin: 0 15px;
  }

  .login-title {
    font-size: 24px;
  }

  .captcha-image {
    width: 120px;
    height: 42px;
  }

  .btn-primary {
    padding: 12px;
  }
}
</style>

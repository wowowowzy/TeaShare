<template>
  <div class="payment-success">
    <!-- 导航栏组件 -->
    <Navbar />

    <!-- 支付成功内容 -->
    <div class="success-content">
      <div class="success-card">
        <div class="success-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <h1 class="success-title">支付成功！</h1>
        <p class="success-message">您的订单已支付成功，感谢您的购买！</p>
        <div class="success-info">
          <p>请前往个人信息页面查看订单号</p>
          <p>支付金额：¥{{ amount }}</p>
          <p>支付时间：{{ paymentTime }}</p>
        </div>
        <div class="success-actions">
          <button class="btn-primary" @click="goToShop">去商城逛逛</button>
          <button class="btn-secondary" @click="goToProfile">查看订单</button>
        </div>
        <div class="countdown" v-if="isValidPaymentSuccessPage">
          <p>页面将在 <span class="countdown-number">{{ countdown }}</span> 秒后自动跳转到商城</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import Navbar from '../../components/Navbar.vue';

const router = useRouter();
const route = useRoute();

// 倒计时
const countdown = ref(5);
// 定时器引用
let countdownTimer = null;

// 订单信息
const orderId = ref('20260325' + Math.floor(Math.random() * 10000));
const amount = ref(route.query.amount || '0.00');
const paymentTime = ref(new Date().toLocaleString());

// 判断是否为有效的支付成功页面
const isValidPaymentSuccessPage = computed(() => {
  // 条件1：路由路径必须为支付成功页面
  const isCorrectPath = route.path === '/payment/success';
  // 条件2：必须有支付金额参数（表示从支付流程跳转）
  const hasValidAmount = route.query.amount && parseFloat(route.query.amount) > 0;
  return isCorrectPath && hasValidAmount;
});

// 清除倒计时定时器
function clearCountdownTimer() {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
}

// 启动倒计时
function startCountdown() {
  // 仅在有效的支付成功页面启动倒计时
  if (!isValidPaymentSuccessPage.value) {
    console.log('非支付成功页面，不启动自动跳转');
    return;
  }

  countdownTimer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearCountdownTimer();
      goToShop();
    }
  }, 1000);
}

// 页面加载时开始倒计时
onMounted(() => {
  startCountdown();
});

// 组件卸载时清除定时器
onUnmounted(() => {
  clearCountdownTimer();
});

// 跳转到商城
function goToShop() {
  clearCountdownTimer();
  router.push('/shop');
}

// 跳转到个人中心
function goToProfile() {
  clearCountdownTimer();
  router.push('/profile');
}
</script>

<style scoped>
.payment-success {
  min-height: 100vh;
  padding-top: 80px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
}

.success-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 60px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
}

.success-card {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 100%;
  width: 480px;
  position: relative;
  overflow: hidden;
}

.success-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #4caf50, #81c784);
}

.success-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 30px;
  background: linear-gradient(135deg, #4caf50, #81c784);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(76, 175, 80, 0.3);
}

.success-icon i {
  font-size: 50px;
  color: white;
}

.success-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px;
}

.success-message {
  font-size: 16px;
  color: #666;
  margin: 0 0 30px;
  line-height: 1.6;
}

.success-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin: 0 0 30px;
  text-align: left;
}

.success-info p {
  font-size: 14px;
  color: #555;
  margin: 8px 0;
  display: flex;
  justify-content: space-between;
}

.success-actions {
  display: flex;
  gap: 16px;
  margin: 0 0 30px;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #4caf50, #81c784);
  color: white;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
}

.btn-secondary {
  background: white;
  color: #4caf50;
  border: 2px solid #4caf50;
}

.btn-secondary:hover {
  background: #f0f7f0;
  transform: translateY(-2px);
}

.countdown {
  font-size: 14px;
  color: #888;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.countdown-number {
  font-size: 18px;
  font-weight: 700;
  color: #4caf50;
  margin: 0 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .success-content {
    padding: 40px 20px;
  }

  .success-card {
    padding: 40px 24px;
    width: 100%;
  }

  .success-icon {
    width: 80px;
    height: 80px;
  }

  .success-icon i {
    font-size: 40px;
  }

  .success-title {
    font-size: 24px;
  }

  .success-actions {
    flex-direction: column;
  }
}
</style>
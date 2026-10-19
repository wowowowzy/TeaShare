<template>
  <div class="order-detail">
    <!-- 加载状态 -->
    <br>
    <br>
    <div v-if="loading" class="loading-container">
      <el-icon class="loading"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path fill-rule="evenodd" d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12Zm13.36-1.814a.75.75 0 1 0-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 0 0-1.06 1.06l2.25 2.25a.75.75 0 0 0 1.14-.094l3.75-5.25Z" clip-rule="evenodd" /></svg></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-icon class="error-icon"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path fill-rule="evenodd" d="M12 2.25c-5.385 0-9.75 4.365-9.75 9.75s4.365 9.75 9.75 9.75 9.75-4.365 9.75-9.75S17.385 2.25 12 2.25Zm-2.625 6c-.54 0-.828.419-.936.634a1.96 1.96 0 0 1-.084.343c-.087.202-.12.423-.12.643 0 .414.336.75.75.75h4.5c.414 0 .75-.336.75-.75 0-.22-.033-.441-.12-.643a1.96 1.96 0 0 1-.084-.343c-.108-.215-.396-.634-.936-.634H9.375Zm.375 7.5a.75.75 0 0 1 .75-.75h3a.75.75 0 0 1 0 1.5h-3a.75.75 0 0 1-.75-.75Z" clip-rule="evenodd" /></svg></el-icon>
      <div class="error-message">{{ error }}</div>
      <el-button type="primary" @click="loadOrderDetail">重新加载</el-button>
    </div>

    <!-- 订单详情内容 -->
    <div v-else-if="order" class="order-content">
      <!-- 订单状态 -->
      <div class="order-status-card">
        <div class="status-icon" :class="getStatusClass(order.status)">
          <el-icon v-if="order.status === 'PENDING_PAYMENT'"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12Zm13.36-1.814a.75.75 0 1 0-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 0 0-1.06 1.06l2.25 2.25a.75.75 0 0 0 1.14-.094l3.75-5.25Z" /></svg></el-icon>
          <el-icon v-else-if="order.status === 'PAID'"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path fill-rule="evenodd" d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12Zm13.36-1.814a.75.75 0 1 0-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 0 0-1.06 1.06l2.25 2.25a.75.75 0 0 0 1.14-.094l3.75-5.25Z" clip-rule="evenodd" /></svg></el-icon>
          <el-icon v-else-if="order.status === 'SHIPPED'"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path d="M19.5 12c0-1.232-.046-2.453-.138-3.662a3.75 3.75 0 0 0-2.725-3.578l-1.723-.467c-1.21-.327-2.517.218-2.844 1.418l-.736 2.883a9.75 9.75 0 0 1-6.633 6.633l-2.883.736c-1.2.327-1.746 1.634-1.418 2.844l.467 1.723a3.75 3.75 0 0 0 3.578 2.725c1.209-.092 2.43-.138 3.662-.138h.375c.414 0 .75-.336.75-.75v-.375Z" /><path d="M16.5 6.75a.75.75 0 0 0-1.5 0v.75h-3v-.75a.75.75 0 0 0-1.5 0v.75h-3v-.75a.75.75 0 0 0-1.5 0v.75h-.75a.75.75 0 0 0 0 1.5H2.25v.75a.75.75 0 0 0 1.5 0v-.75h3v.75a.75.75 0 0 0 1.5 0v-.75h3v.75a.75.75 0 0 0 1.5 0v-.75h3v.75a.75.75 0 0 0 1.5 0v-.75h.75a.75.75 0 0 0 0-1.5h-.75v-.75Z" /></svg></el-icon>
          <el-icon v-else-if="order.status === 'DELIVERED'"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path fill-rule="evenodd" d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12Zm13.36-1.814a.75.75 0 1 0-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 0 0-1.06 1.06l2.25 2.25a.75.75 0 0 0 1.14-.094l3.75-5.25Z" clip-rule="evenodd" /></svg></el-icon>
          <el-icon v-else><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12Zm13.36-1.814a.75.75 0 1 0-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 0 0-1.06 1.06l2.25 2.25a.75.75 0 0 0 1.14-.094l3.75-5.25Z" /></svg></el-icon>
        </div>
        <div class="status-info">
          <div class="status-title">{{ getStatusText(order.status) }}</div>
          <div class="status-desc">{{ getStatusDescription(order.status) }}</div>
        </div>
      </div>

      <!-- 订单基本信息 -->
      <div class="order-info-card">
        <div class="card-header">
          <h3><span class="icon">📦</span> 订单信息</h3>
        </div>
        <div class="card-body">
          <div class="info-item">
            <div class="info-label">订单编号</div>
            <div class="info-value">{{ order.orderId }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">下单时间</div>
            <div class="info-value">{{ formatDate(order.createTime) }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">订单总价</div>
            <div class="info-value">¥{{ (order.totalPrice || 0).toFixed(2) }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">订单状态</div>
            <div class="info-value">{{ getStatusText(order.status) }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">优惠券</div>
            <div class="info-value">{{ order.couponId ? '已使用优惠券' : '未使用优惠券' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">是否支付</div>
            <div class="info-value">{{ order.paymentTime ? formatDate(order.paymentTime) : '已支付' }}</div>
          </div>
        </div>
      </div>

      <!-- 配送地址 -->
      <div class="address-card" v-if="order.address">
        <div class="card-header">
          <h3><span class="icon">📍</span> 配送地址</h3>
        </div>
        <div class="card-body">
          <div class="address-info">
            <div class="address-name">{{ order.address.name }} {{ order.address.phone }}</div>
            <div class="address-detail">{{ order.address.province }} {{ order.address.city }} {{ order.address.district }} {{ order.address.detail }}</div>
          </div>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="products-card">
        <div class="card-header">
          <h3><span class="icon">🛍️</span> 商品信息</h3>
        </div>
        <div class="card-body">
          <div class="product-item" v-for="(product, index) in order.orderDTOList" :key="index">
            <div class="product-image" @click="previewImage(product.goodsImage)">
              <img :src="product.goodsImage" :alt="product.goodsName" />
              <div class="image-overlay">
                <el-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6"><path d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12Zm14.024-.983a1 1 0 0 0-1.41-.014l-3.013 2.959-1.506-1.506a1 1 0 0 0-1.415 1.404l2.12 2.121a1 1 0 0 0 1.415 0l3.715-3.666a1 1 0 0 0 .015-1.41Z" /></svg></el-icon>
              </div>
            </div>
            <div class="product-info">
              <div class="product-name">{{ product.goodsName }}</div>
              <div class="product-intro">{{ product.goodsIntro }}</div>
              <div class="product-spec" v-if="product.specifications">规格：{{ product.specifications }}</div>
            </div>
            <div class="product-price">¥{{ (product.goodsPrice || 0).toFixed(2) }}</div>
            <div class="product-quantity">×{{ product.quantity }}</div>
          </div>
        </div>
      </div>

      <!-- 价格信息 -->
      <div class="price-card">
        <div class="card-header">
          <h3><span class="icon">💰</span> 价格明细</h3>
        </div>
        <div class="card-body">
          <div class="price-item">
            <div class="price-label">商品总价</div>
            <div class="price-value">¥{{ (order.totalPrice || 0).toFixed(2) }}</div>
          </div>
          <div class="price-item total">
            <div class="price-label">实付金额</div>
            <div class="price-value total-price">¥{{ (order.totalPrice || 0).toFixed(2) }}</div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons" v-if="order.status === 'PENDING_PAYMENT'">
        <el-button type="primary" size="large" @click="handlePayment">立即支付</el-button>
        <el-button size="large" @click="cancelOrder">取消订单</el-button>
      </div>
      
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="router.back()">返回订单列表</el-button>
      </div>
    </div>
    
    <!-- 图片预览组件 -->
    <ElImageViewer
      v-if="showImageViewer"
      :url-list="[currentImage]"
      @close="showImageViewer = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElImageViewer } from 'element-plus'
import { getOrderById } from '../../api/user'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => route.query.orderId)
const order = ref(null)
const loading = ref(true)
const error = ref(null)

console.log('订单ID:', orderId.value)

// 图片预览相关
const showImageViewer = ref(false)
const currentImage = ref('')

// 预览图片
const previewImage = (imageUrl) => {
  currentImage.value = imageUrl
  showImageViewer.value = true
}

// 加载订单详情
const loadOrderDetail = async () => {
  console.log('开始加载订单详情，订单ID:', orderId.value)
  
  if (!orderId.value) {
    console.error('订单ID不存在')
    error.value = '订单ID不存在'
    loading.value = false
    return
  }

  loading.value = true
  error.value = null

  try {
    console.log('调用getOrderById，订单ID:', orderId.value)
    const response = await getOrderById(orderId.value)
    console.log('getOrderById返回结果:', response)
    
    if (response) {
      order.value = response
      console.log('订单数据已设置:', order.value)
    } else {
      console.error('获取订单详情失败，响应为空')
      error.value = '获取订单详情失败'
    }
  } catch (err) {
    console.error('获取订单详情失败:', err)
    error.value = '网络错误，获取订单详情失败'
  } finally {
    loading.value = false
    console.log('加载订单详情完成，loading:', loading.value, 'error:', error.value, 'order:', order.value)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取订单状态文本
const getStatusText = (status) => {
  // 支持数字状态码和字符串状态码
  const statusMap = {
    0: '未审核',
    1: '已付款',
    2: '已发货',
    3: '已送达',
    4: '已取消',
    'PENDING_PAYMENT': '待付款',
    'PAID': '已付款',
    'SHIPPED': '已发货',
    'DELIVERED': '已送达',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 获取订单状态描述
const getStatusDescription = (status) => {
  // 支持数字状态码和字符串状态码
  const descMap = {
    0: '订单正在审核中',
    1: '商家正在准备发货',
    2: '商品正在配送中',
    3: '感谢您的购买',
    4: '订单已取消',
    'PENDING_PAYMENT': '请尽快完成支付',
    'PAID': '商家正在准备发货',
    'SHIPPED': '商品正在配送中',
    'DELIVERED': '感谢您的购买',
    'CANCELLED': '订单已取消'
  }
  return descMap[status] || ''
}

// 获取订单状态样式
const getStatusClass = (status) => {
  // 支持数字状态码和字符串状态码
  const classMap = {
    0: 'status-pending',
    1: 'status-paid',
    2: 'status-shipped',
    3: 'status-delivered',
    4: 'status-cancelled',
    'PENDING_PAYMENT': 'status-pending',
    'PAID': 'status-paid',
    'SHIPPED': 'status-shipped',
    'DELIVERED': 'status-delivered',
    'CANCELLED': 'status-cancelled'
  }
  return classMap[status] || ''
}

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const methodMap = {
    'ALIPAY': '支付宝',
    'WECHAT': '微信支付',
    'BANK': '银行转账'
  }
  return methodMap[method] || '其他支付方式'
}

// 处理支付
const handlePayment = () => {
  router.push({
    path: '/pay',
    query: { orderId: order.value.orderId }
  })
}

// 取消订单
const cancelOrder = async () => {
  if (confirm('确定要取消订单吗？')) {
    // 这里应该调用取消订单的API
    ElMessage.success('订单已取消')
    loadOrderDetail()
  }
}

// 生命周期
onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #f8f5f0 0%, #f0ebe4 100%);
  color: #333333;
  padding: 24px;
  min-height: 100vh;
  line-height: 1.5;
  box-sizing: border-box;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
}

.loading-container .loading {
  font-size: 48px;
  color: #4caf50;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  color: #f44336;
}

.error-message {
  font-size: 16px;
  color: #666666;
  max-width: 400px;
}

/* 订单状态卡片 */
.order-status-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  border: 1px solid #e4e6e8;
}

.status-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

.status-pending {
  background: linear-gradient(135deg, #ff9800, #ffb74d);
  color: #ffffff;
}

.status-paid {
  background: linear-gradient(135deg, #2196f3, #64b5f6);
  color: #ffffff;
}

.status-shipped {
  background: linear-gradient(135deg, #4caf50, #81c784);
  color: #ffffff;
}

.status-delivered {
  background: linear-gradient(135deg, #9c27b0, #ba68c8);
  color: #ffffff;
}

.status-cancelled {
  background: linear-gradient(135deg, #607d8b, #90a4ae);
  color: #ffffff;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 20px;
  font-weight: 600;
  color: #111111;
  margin-bottom: 8px;
}

.status-desc {
  font-size: 14px;
  color: #666666;
}

/* 通用卡片样式 */
.order-info-card,
.address-card,
.products-card,
.price-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  border: 1px solid #e4e6e8;
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f2f5;
  background: linear-gradient(to right, #f9f9f9, #ffffff);
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #111111;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .icon {
  font-size: 16px;
}

.card-body {
  padding: 24px;
}

/* 信息项样式 */
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #333333;
  font-weight: 600;
}

/* 地址信息 */
.address-info {
  line-height: 1.6;
}

.address-name {
  font-weight: 600;
  color: #111111;
  margin-bottom: 8px;
}

.address-detail {
  font-size: 14px;
  color: #666666;
}

/* 商品信息 */
.product-item {
  display: flex;
  align-items: flex-start;
  padding: 20px 0;
  border-bottom: 1px solid #f0f2f5;
  transition: all 0.3s ease;
  gap: 16px;
}

.product-item:hover {
  background: #f5f5f7;
  padding-left: 16px;
  padding-right: 16px;
  margin: 0 -16px;
  border-radius: 12px;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.product-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  color: #ffffff;
  font-size: 24px;
}

.product-image:hover .image-overlay {
  opacity: 1;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 16px;
  color: #111111;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.4;
}

.product-intro {
  font-size: 14px;
  color: #666666;
  margin-bottom: 8px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-spec {
  font-size: 12px;
  color: #999999;
}

.product-price {
  font-size: 16px;
  color: #e53935;
  font-weight: 600;
  min-width: 100px;
  text-align: right;
  margin-top: 8px;
}

.product-quantity {
  font-size: 14px;
  color: #666666;
  min-width: 80px;
  text-align: right;
  margin-top: 8px;
}

/* 价格信息 */
.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.price-item:last-child {
  border-bottom: none;
}

.price-label {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
}

.price-value {
  font-size: 14px;
  color: #333333;
  font-weight: 600;
}

.price-discount {
  color: #e53935;
}

.price-item.total {
  padding-top: 16px;
  margin-top: 8px;
  border-top: 2px solid #f0f2f5;
}

.total-price {
  font-size: 20px;
  color: #e53935;
  font-weight: 700;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 32px;
  padding: 24px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e6e8;
}

/* 返回按钮 */
.back-button {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  margin-bottom: 32px;
}

.back-button button {
  transition: all 0.3s ease;
}

.back-button button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-detail {
    padding: 16px;
  }

  .order-status-card {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }

  .card-header {
    padding: 16px 20px;
  }

  .card-body {
    padding: 20px;
  }

  .product-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .product-image {
    width: 100%;
    max-width: 200px;
    height: auto;
  }

  .product-price,
  .product-quantity {
    align-self: flex-start;
    margin-top: 4px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons button {
    width: 100%;
  }

  .back-button {
    margin-top: 16px;
    margin-bottom: 24px;
  }
}

@media (max-width: 480px) {
  .order-status-card {
    padding: 16px;
  }

  .status-icon {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }

  .status-title {
    font-size: 18px;
  }

  .product-image {
    max-width: 150px;
  }

  .product-name {
    font-size: 14px;
  }

  .product-intro {
    font-size: 12px;
  }

  .product-price {
    font-size: 14px;
  }
}
</style>
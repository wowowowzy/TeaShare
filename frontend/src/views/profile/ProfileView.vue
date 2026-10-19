<template>
  <div class="personal-center">
    <br>
    <br>
    <!-- 个人信息卡片 -->
    <div class="card profile-card">
      <div class="card-header">
        <h2><span class="icon">👤</span> 个人信息</h2>
      </div>
      <div class="card-body">
        <div class="profile">
          <div class="avatar">
            <img :src="userInfo.avatar" alt="" class="avatar-image" @error="handleAvatarError">
            <div class="avatar-upload">
              <input type="file" id="avatar-input" class="avatar-input" accept="image/*" @change="handleAvatarChange">
              <label for="avatar-input" class="avatar-upload-btn">更换头像</label>
            </div>
          </div>
          <div class="profile-details">
            <div class="detail-item">
              <div class="detail-label">用户ID</div>
              <div class="detail-value">{{ userInfo.userId }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">用户名</div>
              <div class="detail-value">{{ userInfo.userName }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">手机号码</div>
              <div class="detail-value">{{ userInfo.phone }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">注册时间</div>
              <div class="detail-value">{{ userInfo.createTime ? userInfo.createTime.split('T')[0] : '' }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">审核状态</div>
              <div class="detail-value" :class="getAuditStatusClass(userInfo.status)">
                {{ getAuditStatusText(userInfo.status) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能模块网格 -->
    <div class="grid">
      <!-- 地址管理 -->
      <div class="card address-card">
        <div class="card-header" @click="toggleAddressExpand">
          <h2><span class="icon">📍</span> 收货地址</h2>
          <div class="header-actions">
            <button class="btn-add" @click.stop="openAddAddressDialog">添加地址</button>
            <!-- <span class="expand-icon">{{ addressExpanded ? '▲' : '▼' }}</span> -->
          </div>
        </div>
        <div class="card-body" v-show="addressExpanded">
          <div class="address-list" v-if="addresses.length > 0">
            <div class="address" v-for="address in addresses" :key="address.id">
              <div class="address-header">
                <span class="address-name">{{ address.name }}</span>
                <span class="address-phone">{{ address.phone }}</span>
                <span class="default-badge" v-if="address.isDefault == 1">默认</span>
              </div>
              <div class="address-content">{{ address.province }} {{ address.city }} {{ address.district }} {{
                address.detail }}</div>
              <div class="address-actions">
                <button class="btn btn-edit" @click="editAddress(address)">编辑</button>
                <button class="btn btn-delete" :disabled="address.isDefault"
                  @click="deleteAddress(address.id)">删除</button>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <div class="empty-icon">📍</div>
            <div class="empty-text">暂无地址</div>
            <button class="btn-add-empty" @click="openAddAddressDialog">添加收货地址</button>
          </div>
        </div>
      </div>

      <!-- 优惠券 -->
      <div class="card coupon-card">
        <div class="card-header" @click="toggleCouponExpand">
          <h2><span class="icon">🎫</span> 我的优惠券</h2>
          <div class="header-actions">
            <span v-if="coupons.length > 2" class="expand-icon">{{ couponExpanded ? '▲' : '▼' }}</span>
          </div>
        </div>
        <div class="card-body">
          <div class="coupon-list" v-if="coupons.length > 0">
            <div class="coupon" v-for="(coupon, index) in coupons" :key="coupon.id" v-show="index < 2 || couponExpanded">
              <div class="coupon-value">
                <div class="coupon-amount">¥{{ coupon.reduceAmount }}</div>
              </div>
              <div class="coupon-info">
                <div class="coupon-name">{{ coupon.intro }}</div>
                <div class="coupon-date">有效期至: {{ coupon.endTime }}</div>
              </div>
            </div>
          </div>
          <div v-if="coupons.length > 2 && !couponExpanded" class="expand-hint" @click="toggleCouponExpand">
            展开查看全部 {{ coupons.length }} 张优惠券
          </div>
          <div v-else-if="coupons.length === 0" class="empty-state">
            <div class="empty-icon">🎫</div>
            <div class="empty-text">暂无优惠券</div>
            <button class="btn-get-coupon" @click="goToShop">去领取优惠券</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单管理 -->
    <div class="card order-card">
      <div class="card-header">
        <h2><span class="icon">📦</span> 我的订单</h2>
      </div>
      <div class="card-body">
        <div class="orders">
          <div class="order-list">
            <div class="order-card" v-for="order in orders" :key="order.orderId" @click="handleOrderClick(order)">
              <!-- 订单头部 -->
              <div class="order-header">
                <div class="order-number">订单号 {{ order.orderId }}</div>
                <div class="order-status" :class="getOrderStatusClass(order.status)">
                  {{ getOrderStatusText(order.status) }}
                </div>
              </div>

              <!-- 商品清单 -->
              <div class="order-products">
                <div class="product-item" v-for="(product, index) in order.orderDTOList" :key="index">
                  <div class="product-main">
                    <div class="product-name">{{ product.goodsName }}</div>
                    <div class="product-details">
                      <span class="product-price">¥{{ (product.goodsPrice || 0).toFixed(2) }}</span>
                      <span class="product-multiply">×</span>
                      <span class="product-quantity">{{ product.quantity }}</span>
                    </div>
                  </div>
                  <div class="product-subtotal">
                    小计：<span class="subtotal-amount">¥{{ ((product.goodsPrice || 0) * (product.quantity || 0)).toFixed(2) }}</span>
                  </div>
                </div>
              </div>

              <!-- 订单底部 -->
              <div class="order-footer">
                <div class="order-info-row">
                  <div class="order-time">
                    <span class="time-icon">🕐</span>
                    <span class="time-label">下单时间：</span>
                    <span class="time-value">{{ order.createTime ? order.createTime.split('T')[0] : '' }}</span>
                  </div>
                  <div class="order-number-display">
                    <span class="number-icon"></span>
                    <span class="number-label">订单号：</span>
                    <span class="number-value">{{ order.orderId }}</span>
                  </div>
                </div>
                <div class="order-total-row">
                  <div class="order-count">共 {{ getOrderProductCount(order) }} 件商品</div>
                  <div class="order-total">
                    <span class="total-label">订单总价：</span>
                    <span class="total-amount">¥{{ (order.totalPrice || calculateOrderTotal(order)).toFixed(2) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="orders.length === 0" class="empty-state">
              <div class="empty-icon">📦</div>
              <div class="empty-text">暂无订单</div>
              <button class="btn-go-shopping" @click="goToShop">去商城逛逛</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加地址对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑收货地址' : '添加收货地址'" width="500px">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef">
        <el-form-item label="收件人姓名" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收件人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份"></el-input>
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市"></el-input>
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县"></el-input>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" placeholder="请输入详细地址"></el-input>
        </el-form-item>
        <el-form-item label="邮政编码" prop="postalCode">
          <el-input v-model="addressForm.postalCode" placeholder="请输入邮政编码"></el-input>
        </el-form-item>
        <el-form-item label="是否默认地址" prop="isDefault">
          <el-radio-group v-model="addressForm.isDefault">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddressForm">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserShow, getOrders, getAddressList, getCoupons, updateAddress, deleteAddress as deleteAddressApi, addAddress, uploadAvatar } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { cartApi } from '../../api/cart'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userInfo = reactive({})

// 订单数据
const orders = ref([])

// 地址数据
const addresses = ref([])

// 优惠券数据
const coupons = ref([])

// 下拉列表展开状态
const addressExpanded = ref(true)
const couponExpanded = ref(true)

// 添加地址对话框
const dialogVisible = ref(false)
const addressFormRef = ref(null)
const isEditing = ref(false)
const currentAddressId = ref(null)

const addressForm = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  postalCode: '',
  isDefault: 0
})

// 表单验证规则
const addressRules = ref({
  name: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码格式', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区县', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '详细地址长度应在5-200个字符之间', trigger: 'blur' }
  ],
  postalCode: [
    { pattern: /^\d{6}$/, message: '请输入正确的6位邮政编码', trigger: 'blur' }
  ],
  isDefault: [
    { required: true, message: '请选择是否默认地址', trigger: 'change' }
  ]
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 首先调用userStore的getUserShow方法，更新全局用户信息
    await userStore.getUserShow()
    console.log('用户信息:', userStore.userInfo)
    // 从userStore中获取更新后的用户信息
    const userData = userStore.userInfo
    if (userData) {
      // 获取时间戳，用于避免头像缓存
      const timestamp = localStorage.getItem('avatarTimestamp') || Date.now().toString()
      // 处理头像URL，添加时间戳参数
      let avatarUrl = userData.avatar || ''
      // 如果没有头像，使用默认头像
      if (!avatarUrl) {
        avatarUrl = `/assest/default.jpg?t=${timestamp}`
      } else {
        // 检查URL是否已经包含查询参数
        if (avatarUrl.includes('?')) {
          // 已经有查询参数，添加时间戳
          avatarUrl = `${avatarUrl}&t=${timestamp}`
        } else {
          // 没有查询参数，添加时间戳作为第一个参数
          avatarUrl = `${avatarUrl}?t=${timestamp}`
        }
      }
      
      Object.assign(userInfo, {
        userId: userData.userId || userData.id || '',
        userName: userData.userName || userData.username || '',
        phone: userData.phone || '',
        avatar: avatarUrl,
        createTime: userData.createTime || '',
        status: userData.status || null
      })
      // console.log('用户信息:', userInfo)
      console.log('用户审核状态:', userInfo.status)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    showError('获取用户信息失败')
  }
}

// 加载订单
const loadOrders = async () => {
  try {
    const ordersData = await getOrders()
    if (Array.isArray(ordersData)) {
      orders.value = ordersData
    } else if (ordersData && ordersData.data && Array.isArray(ordersData.data)) {
      orders.value = ordersData.data
    } else {
      orders.value = []
    }
    console.log('订单数据:', orders.value)
  } catch (error) {
    console.error('获取订单信息失败:', error)
    showError('获取订单信息失败')
    orders.value = []
  }
}

// 加载地址
const loadAddresses = async () => {
  try {
    const addressesData = await getAddressList()
    if (Array.isArray(addressesData)) {
      addresses.value = addressesData
    } else if (addressesData && addressesData.data && Array.isArray(addressesData.data)) {
      addresses.value = addressesData.data
    } else {
      addresses.value = []
    }
    console.log('地址数据:', addresses.value)
  } catch (error) {
    console.error('获取地址信息失败:', error)
    showError('获取地址信息失败')
    addresses.value = []
  }
}

// 加载优惠券
const loadCoupons = async () => {
  try {
    const couponsData = await getCoupons()
    if (Array.isArray(couponsData)) {
      coupons.value = couponsData
    } else if (couponsData && couponsData.data && Array.isArray(couponsData.data)) {
      coupons.value = couponsData.data
    } else {
      coupons.value = []
    }
    console.log('优惠券数据:', coupons.value)
  } catch (error) {
    console.error('获取优惠券信息失败:', error)
    showError('获取优惠券信息失败')
    coupons.value = []
  }
}

// 添加商品到购物车
const addToCart = async (goodsId, quantity) => {
  try {
    if (!goodsId || quantity <= 0) {
      showError('商品ID和数量不能为空')
      return false
    }
    
    const response = await cartApi.addToCart(goodsId, quantity)
    
    if (response && response.code === 1) {
      ElMessage.success('商品已成功添加到购物车')
      return true
    } else {
      showError(response?.msg || '添加购物车失败')
      return false
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
    showError('添加购物车失败，请稍后重试')
    return false
  }
}

// 切换地址展开状态
const toggleAddressExpand = () => {
  addressExpanded.value = !addressExpanded.value
}

// 切换优惠券展开状态
const toggleCouponExpand = () => {
  couponExpanded.value = !couponExpanded.value
}

// 打开添加地址对话框
const openAddAddressDialog = () => {
  Object.assign(addressForm, {
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    postalCode: '',
    isDefault: 0
  })
  isEditing.value = false
  currentAddressId.value = null
  dialogVisible.value = true
}

// 提交地址表单
const submitAddressForm = async () => {
  if (!addressFormRef.value) {
    showError('表单初始化失败')
    return
  }

  try {
    await addressFormRef.value.validate()

    let response
    if (isEditing.value) {
      // 编辑模式
      response = await updateAddress({ ...addressForm, id: currentAddressId.value })
    } else {
      // 添加模式
      response = await addAddress(addressForm)
    }

    if (response && response.code === 1) {
      ElMessage.success(isEditing.value ? '地址修改成功' : '地址添加成功')
      dialogVisible.value = false
      loadAddresses()
      isEditing.value = false
      currentAddressId.value = null
    } else {
      showError(isEditing.value ? '地址修改失败' : '地址添加失败')
    }
  } catch (error) {
    console.error('提交地址表单失败:', error)
    if (error.name === 'Error') {
      showError(error.message)
    } else {
      showError('表单验证失败')
    }
  }
}

// 编辑地址
const editAddress = (address) => {
  isEditing.value = true
  currentAddressId.value = address.id

  Object.assign(addressForm, {
    name: address.name || '',
    phone: address.phone || '',
    province: address.province || '',
    city: address.city || '',
    district: address.district || '',
    detail: address.detail || '',
    postalCode: address.postalCode || '',
    isDefault: address.isDefault || 0
  })

  dialogVisible.value = true
}

// 删除地址
const deleteAddress = async (id) => {
  if (!confirm('确定要删除这个地址吗？')) {
    return
  }

  try {
    const response = await deleteAddressApi(id)
    if (response && response.code === 1) {
      ElMessage.success('地址删除成功')
      loadAddresses()
    } else {
      showError('地址删除失败')
    }
  } catch (error) {
    console.error('删除地址失败:', error)
    showError('删除地址失败')
  }
}

// 显示错误信息
const showError = (message) => {
  ElMessage({
    message: message,
    type: 'error',
    showClose: true,
    duration: 3000
  })
}

// 订单点击事件处理
const handleOrderClick = async (order) => {
  try {
    // 跳转到订单详情页
    router.push({
      path: '/order/detail',
      query: {
        orderId: order.orderId
      }
    })
  } catch (error) {
    console.error('跳转到订单详情页失败:', error)
    showError('跳转到订单详情页失败')
  }
}

// 计算订单总价
const calculateOrderTotal = (order) => {
  if (!order || !order.orderDTOList || order.orderDTOList.length === 0) {
    return 0
  }

  let total = 0
  for (const item of order.orderDTOList) {
    const price = parseFloat(item.goodsPrice) || 0
    const quantity = parseInt(item.quantity) || 0
    total += price * quantity
  }

  return total
}

// 计算订单商品数量
const getOrderProductCount = (order) => {
  if (!order || !order.orderDTOList || order.orderDTOList.length === 0) {
    return 0
  }

  let count = 0
  for (const item of order.orderDTOList) {
    count += parseInt(item.quantity) || 0
  }

  return count
}

// 跳转到商城
const goToShop = () => {
  router.push('/shop')
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  // 支持数字状态码和字符串状态码
  const statusMap = {
    0: '未审核',
    1: '已审核',
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

// 获取订单状态样式
const getOrderStatusClass = (status) => {
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

// 处理头像上传
const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    // 验证文件类型
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      showError('请选择图片文件（JPEG、PNG、GIF、WebP）')
      return
    }

    // 验证文件大小（限制为5MB）
    const maxSize = 5 * 1024 * 1024
    if (file.size > maxSize) {
      showError('图片大小不能超过5MB')
      return
    }

    // 上传头像
    const response = await uploadAvatar(file)
    // 由于响应拦截器的处理，成功时会直接返回data，而不是整个响应对象
    // 所以只要response存在，就说明上传成功
    if (response) {
      ElMessage.success('头像上传成功')
      // 重新加载用户信息以更新头像
      // 为了确保获取最新的头像信息，我们可以在URL后添加时间戳来避免缓存
      localStorage.setItem('avatarTimestamp', Date.now().toString())
      loadUserInfo()
    } else {
      showError('头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    showError('头像上传失败，请稍后重试')
  } finally {
    // 清空文件输入，允许重复选择同一个文件
    event.target.value = ''
  }
}

// 处理头像加载失败
const handleAvatarError = (event) => {
  // 当头像加载失败时，使用默认头像
  const timestamp = Date.now().toString()
  event.target.src = `/assest/default.jpg?t=${timestamp}`
  console.log('头像加载失败，使用默认头像')
}

// 获取审核状态文本
const getAuditStatusText = (status) => {
  // 当status为0时显示"未审核"，为1时显示"已审核"
  if (status === 0) {
    return '未审核'
  } else if (status === 1) {
    return '已审核'
  } else {
    return '未知状态'
  }
}

// 获取审核状态样式
const getAuditStatusClass = (status) => {
  if (status === 0) {
    return 'status-pending'
  } else if (status === 1) {
    return 'status-approved'
  } else {
    return ''
  }
}

// 生命周期
onMounted(() => {
  loadUserInfo()
  loadOrders()
  loadAddresses()
  loadCoupons()
})
</script>

<style scoped>
/* 基础样式 */
.personal-center {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #f8f5f0 0%, #f0ebe4 100%);
  color: #333333;
  padding: 24px;
  min-height: 100vh;
  line-height: 1.5;
  box-sizing: border-box;
}

/* 卡片基础样式 */
.card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #e4e6e8;
  margin-bottom: 20px;
}

.card:hover {
  box-shadow: 0 6px 20px rgba(46, 125, 50, 0.15);
  transform: translateY(-2px);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(to right, #f9f9f9, #ffffff);
  cursor: pointer;
  user-select: none;
}

.card-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #111111;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.card-header .icon {
  font-size: 16px;
}

.card-body {
  padding: 24px;
}



/* 网格布局 */
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

/* 个人信息卡片 */
.profile {
  display: flex;
  align-items: center;
  gap: 32px;
  position: relative;
}

.avatar {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  position: relative;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  transition: all 0.3s ease;
}

.avatar:hover .avatar-image {
  transform: scale(1.05);
  border-color: #4caf50;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  text-align: center;
  opacity: 0;
  transition: all 0.3s ease;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 0 0 50% 50%;
  padding: 8px 0;
  cursor: pointer;
}

.avatar:hover .avatar-upload {
  opacity: 1;
}

.avatar-input {
  display: none;
}

.avatar-upload-btn {
  color: #ffffff;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-upload-btn:hover {
  color: #4caf50;
  text-decoration: underline;
}

.profile-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  flex: 1;
}

@media (max-width: 768px) {
  .profile {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }

  .profile-details {
    grid-template-columns: 1fr;
    gap: 16px;
    width: 100%;
  }

  .avatar {
    width: 100px;
    height: 100px;
  }
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  background: #f5f5f7;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.detail-item:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.detail-label {
  color: #666666;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 15px;
  font-weight: 600;
  color: #333333;
}

/* 审核状态样式 */
.status-pending {
  /* color: #ff9800 !important; */
  background: rgba(255, 152, 0, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.status-approved {
  color: #4caf50 !important;
  background: rgba(76, 175, 80, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

/* 地址管理 */
.address-list {
  /* display: flex; */
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.address {
  background: #ffffff;
  border: 2px solid #f0f2f5;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  margin-bottom: 10px;
}

.address:hover {
  border-color: #eaf1ea;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.address-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
}

.address-name {
  font-weight: 600;
  color: #111111;
  font-size: 16px;
}

.address-phone {
  color: #666666;
  font-size: 14px;
  font-family: 'Consolas', 'Monaco', monospace;
}

.default-badge {
  background: linear-gradient(135deg, #2196f3, #64b5f6);
  color: #ffffff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 20px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(33, 150, 243, 0.2);
}

.address-content {
  font-size: 14px;
  color: #333333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.address-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px dashed #e0e0e0;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: #f5f5f7;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 12px;
  color: #333333;
  font-weight: 500;
  text-decoration: none;
  display: inline-block;
}

.btn:hover {
  background: #e0e0e0;
  transform: translateY(-1px);
}

.btn-edit {
  color: #333333;
}

.btn-delete {
  color: #f44336;
}

.btn-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 优惠券样式 */
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding-right: 8px;
}

.coupon {
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border-radius: 12px;
  padding: 0;
  display: flex;
  position: relative;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  min-height: 100px;
}

.coupon:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #ff9800;
}

.coupon-value {
  background: linear-gradient(135deg, #ff7043, #ff9800);
  padding: 20px;
  min-width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.coupon-value::after {
  content: '';
  position: absolute;
  right: -5px;
  top: 0;
  width: 10px;
  height: 100%;
  background: repeating-linear-gradient(45deg,
      transparent,
      transparent 5px,
      #ffffff 5px,
      #ffffff 10px);
}

.coupon-amount {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  line-height: 1;
}

.coupon-info {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-name {
  font-weight: 600;
  font-size: 16px;
  color: #111111;
  margin-bottom: 4px;
}

.coupon-date {
  font-size: 12px;
  color: #666666;
}

.expand-hint {
  text-align: center;
  padding: 16px;
  color: #4caf50;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  border-top: 1px dashed #e0e0e0;
  margin-top: 12px;
  transition: all 0.3s ease;
}

.expand-hint:hover {
  color: #2e7d32;
  background: rgba(76, 175, 80, 0.05);
}

/* 订单管理 */
.order-list {
  /* display: flex; */
  flex-direction: column;
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
  padding-bottom: 2px;
}

/* 按钮样式 */
.btn-add {
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  color: #ffffff;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
  white-space: nowrap;
  text-decoration: none;
}

.btn-add:hover {
  background: linear-gradient(135deg, #1b5e20, #2e7d32);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(46, 125, 50, 0.4);
}

.btn-add-empty {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  color: #ffffff;
  border: none;
  padding: 12px 24px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
  margin-top: 16px;
  text-decoration: none;
}

.btn-get-coupon {
  display: inline-block;
  background: linear-gradient(135deg, #ff9800, #ffb74d);
  color: #ffffff;
  border: none;
  padding: 12px 24px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
  text-decoration: none;
}

.btn-go-shopping {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  color: #ffffff;
  border: none;
  padding: 12px 24px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
  text-decoration: none;
}

.btn-go-shopping:hover,
.btn-get-coupon:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(46, 125, 50, 0.4);
}

/* 链接样式 */
.link {
  font-size: 12px;
  color: #2e7d32;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 8px;
}

.link:hover {
  color: #1b5e20;
  background: rgba(46, 125, 50, 0.1);
  transform: translateX(2px);
}

.expand-icon {
  color: #666666;
  font-size: 12px;
  transition: transform 0.3s ease;
  margin-left: 8px;
  width: 12px;
  text-align: center;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #666666;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5f5f7, #f0f0f0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #666666;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.empty-text {
  font-size: 15px;
  color: #666666;
  font-weight: 500;
}

/* 滚动条样式 */
.address-list::-webkit-scrollbar,
.coupon-list::-webkit-scrollbar,
.order-list::-webkit-scrollbar {
  width: 6px;
}

.address-list::-webkit-scrollbar-track,
.coupon-list::-webkit-scrollbar-track,
.order-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.address-list::-webkit-scrollbar-thumb,
.coupon-list::-webkit-scrollbar-thumb,
.order-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.address-list::-webkit-scrollbar-thumb:hover,
.coupon-list::-webkit-scrollbar-thumb:hover,
.order-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .personal-center {
    padding: 16px;
  }

  .card-header {
    padding: 16px 20px;
  }

  .card-body {
    padding: 20px;
  }

  .card-header h2 {
    font-size: 16px;
  }

  .btn-add {
    padding: 8px 16px;
    font-size: 12px;
  }

  .v-order-header {
    padding: 16px 20px;
  }

  .v-order-content {
    padding: 20px;
  }

  .v-total-amount {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .grid {
    gap: 16px;
  }

  .profile {
    flex-direction: column;
    text-align: center;
  }

  .profile-details {
    grid-template-columns: 1fr;
  }

  .avatar {
    width: 100px;
    height: 100px;
  }

  .avatar-image {
    width: 100px;
    height: 100px;
  }

  .v-order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .v-product-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .v-product-quantity {
    align-self: flex-start;
  }
}

.order-list .order-card {
  margin-bottom: 30px;
}

.order-list .order-card:last-child {
  margin-bottom: 0;
}
/* 新订单管理样式 */
.order-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e4e6e8;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #c6cfc6;
}

.order-card:hover::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  /* background: linear-gradient(90deg, #2e7d32, #8bc34a); */
}

.order-header {
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  padding: 20px 24px;
  border-bottom: 1px solid #e4e6e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.order-number {
  font-size: 16px;
  font-weight: 600;
  color: #111111;
  font-family: 'Consolas', 'Monaco', monospace;
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-number::before {
  content: '📦';
  font-size: 14px;
}

.order-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  min-width: 80px;
  transition: all 0.3s ease;
}

.status-pending {
  background: linear-gradient(135deg, #ff9800, #ffb74d);
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(255, 152, 0, 0.2);
}

.status-paid {
  background: linear-gradient(135deg, #2196f3, #64b5f6);
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(33, 150, 243, 0.2);
}

.status-shipped {
  background: linear-gradient(135deg, #4caf50, #81c784);
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(76, 175, 80, 0.2);
}

.status-delivered {
  background: linear-gradient(135deg, #9c27b0, #ba68c8);
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(156, 39, 176, 0.2);
}

.status-cancelled {
  background: linear-gradient(135deg, #607d8b, #90a4ae);
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(96, 125, 139, 0.2);
}

.order-products {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f2f5;
}

.product-item {
  display: flex;
  flex-direction: column;
  padding: 16px 0;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f2f5;
}

.product-item:last-child {
  border-bottom: none;
}

.product-item:hover {
  background: #f5f5f7;
  padding-left: 12px;
  padding-right: 12px;
  margin: 0 -12px;
  border-radius: 12px;
}

.product-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.product-name {
  font-size: 15px;
  color: #111111;
  font-weight: 500;
  flex: 1;
  padding-right: 16px;
  line-height: 1.4;
}

.product-details {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-price {
  font-size: 14px;
  color: #e53935;
  font-weight: 600;
}

.product-multiply {
  font-size: 12px;
  color: #999999;
}

.product-quantity {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
}

.product-subtotal {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 13px;
  color: #666666;
}

.subtotal-amount {
  font-size: 15px;
  color: #e53935;
  font-weight: 600;
}

.order-footer {
  padding: 20px 24px;
  background: #f9f9f9;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.order-time {
  display: flex;
  align-items: center;
  color: #666666;
  font-size: 13px;
  gap: 4px;
}

.time-icon {
  font-size: 14px;
}

.time-label {
  font-size: 13px;
  color: #666666;
}

.time-value {
  font-size: 13px;
  color: #333333;
  font-weight: 500;
}

.order-number-display {
  display: flex;
  align-items: center;
  color: #666666;
  font-size: 13px;
  gap: 4px;
}

.number-icon {
  font-size: 14px;
}

.number-label {
  font-size: 13px;
  color: #666666;
}

.number-value {
  font-size: 13px;
  color: #333333;
  font-weight: 500;
  font-family: 'Consolas', 'Monaco', monospace;
}

.order-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e0e0e0;
}

.order-count {
  font-size: 13px;
  color: #666666;
}

.order-total {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.total-label {
  font-size: 14px;
  color: #333333;
  font-weight: 500;
}

.total-amount {
  font-size: 20px;
  font-weight: 700;
  color: #e53935;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.total-amount::before {
  content: '¥';
  font-size: 14px;
  font-weight: 500;
  color: #e53935;
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .order-footer {
    gap: 12px;
  }
  
  .order-info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .order-total-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .order-status {
    align-self: flex-start;
  }
  
  .product-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .product-details {
    width: 100%;
    justify-content: space-between;
  }
  
  .product-subtotal {
    width: 100%;
    justify-content: space-between;
  }
}
</style>

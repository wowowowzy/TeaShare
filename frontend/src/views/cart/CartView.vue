<template>
  <div class="cart">
    <!-- 导航栏组件 -->
    <Navbar />

    <!-- 购物车内容 -->
    <div class="cart-content">
      <!-- 顶部标题栏 -->
      <div class="cart-header">
        <h1 class="cart-title">我的茶品购物车</h1>
        <button class="clear-cart-btn" @click="clearCart">清空购物车</button>
      </div>
      
      <!-- 购物车为空 -->
      <div v-if="loading" class="loading-cart">
        <p>加载中...</p>
      </div>
      <div v-else-if="cartItems.length === 0" class="empty-cart">
        <p>购物车为空</p>
        <router-link to="/shop" class="btn btn-primary">去购物</router-link>
      </div>
      
      <!-- 购物车商品 -->
      <div v-else>
        <div class="cart-items-container">
          <div class="cart-items">
            <div class="cart-item" v-for="item in cartItems" :key="item.goodsId">
              <div class="cart-item-image">
                <img :src="item.image" :alt="item.name" />
              </div>
              <div class="cart-item-info">
                <h3>{{ item.name }}</h3>
                <p class="cart-item-specs">{{ item.specs }}</p>
                <p class="cart-item-price">¥{{ item.price.toFixed(2) }}</p>
              </div>
              <div class="cart-item-actions">
                <div class="quantity-control">
                  <button @click="decreaseQuantity(item)" class="quantity-btn">-</button>
                  <span class="quantity">{{ item.quantity }}</span>
                  <button @click="increaseQuantity(item)" class="quantity-btn">+</button>
                </div>
                <button class="delete-btn" @click="removeFromCart(item)">删除</button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 底部总计栏 -->
        <div class="cart-footer">
          <div class="total-section">
            <span class="total-label">总计：</span>
            <span class="total-price">¥{{ (totalPrice + shippingFee).toFixed(2) }}</span>
          </div>
          <button class="checkout-btn" @click="checkout">去结算</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Navbar from '../../components/Navbar.vue';
import { cartApi } from '../../api/cart';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();

// 购物车数据
const cartItems = ref([]);
const shippingFee = ref(0);
const loading = ref(false);

// 计算总价
const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => {
    if (item && typeof item.price === 'number' && typeof item.quantity === 'number') {
      return total + item.price * item.quantity;
    }
    return total;
  }, 0);
});

// 获取默认规格信息
function getDefaultSpecs(name) {
  const specsMap = {
    "西湖龙井": "规格：250g/罐 | 产地：浙江杭州 | 采摘时间：2025年春",
    "普洱": "规格：357g/饼 | 年份：2018年 | 产地：云南西双版纳",
    "铁观音": "规格：500g/盒 | 等级：一级 | 产地：福建安溪",
    "凤凰单丛": "规格：250g/袋 | 产地：广东潮州 | 等级：特级"
  };
  
  for (const key in specsMap) {
    if (name.includes(key)) {
      return specsMap[key];
    }
  }
  
  return "规格：250g/袋 | 产地：中国 | 等级：一级";
}

// 获取购物车列表
async function getCartList() {
  loading.value = true;
  try {
    const data = await cartApi.getCartList();
    console.log('购物车数据:', data);
    // 处理数据，添加规格信息
    cartItems.value = (data || []).map(item => ({
      ...item,
      // 确保必要字段存在，映射API返回的字段名
      goodsId: item.goodsId || item.id,
      name: item.goodsName || item.name || '未知商品',
      price: typeof item.goodsPrice === 'number' ? item.goodsPrice : (typeof item.price === 'number' ? item.price : 0),
      quantity: typeof item.quantity === 'number' ? item.quantity : 1,
      image: item.goodsImage || item.image || 'https://picsum.photos/800/800?random=' + Math.floor(Math.random() * 100),
      specs: item.goodsIntro || item.specs || getDefaultSpecs(item.goodsName || item.name || '')
    }));
  } catch (error) {
    console.error('获取购物车失败:', error);
    ElMessage.error('获取购物车失败');
    // 使用模拟数据
    cartItems.value = [
      {
        goodsId: 1,
        name: "西湖龙井 明前特级",
        price: 298,
        quantity: 1,
        image: "https://picsum.photos/800/800?random=20",
        specs: "规格：250g/罐 | 产地：浙江杭州 | 采摘时间：2025年春"
      },
      {
        goodsId: 2,
        name: "云南普洱熟茶 七子饼茶",
        price: 168,
        quantity: 2,
        image: "https://picsum.photos/800/800?random=21",
        specs: "规格：357g/饼 | 年份：2018年 | 产地：云南西双版纳"
      },
      {
        goodsId: 3,
        name: "安溪铁观音 清香型",
        price: 128,
        quantity: 1,
        image: "https://picsum.photos/800/800?random=22",
        specs: "规格：500g/盒 | 等级：一级 | 产地：福建安溪"
      }
    ];
  } finally {
    loading.value = false;
  }
}

// 减少数量
async function decreaseQuantity(item) {
  if (item.quantity > 1) {
    item.quantity--;
    try {
      await cartApi.updateCartItem(item.goodsId, item.quantity);
    } catch (error) {
      console.error('更新购物车失败:', error);
      ElMessage.error('更新失败');
      // 恢复原来的数量
      item.quantity++;
    }
  }
}

// 增加数量
async function increaseQuantity(item) {
  item.quantity++;
  try {
    await cartApi.updateCartItem(item.goodsId, item.quantity);
  } catch (error) {
    console.error('更新购物车失败:', error);
    ElMessage.error('更新失败');
    // 恢复原来的数量
    item.quantity--;
  }
}

// 从购物车中移除
async function removeFromCart(item) {
  try {
    await cartApi.deleteCartItems([item.goodsId]);
    cartItems.value = cartItems.value.filter(cartItem => cartItem.goodsId !== item.goodsId);
    ElMessage.success('删除成功');
  } catch (error) {
    console.error('删除购物车商品失败:', error);
    ElMessage.error('删除失败');
  }
}

// 清空购物车
async function clearCart() {
  if (cartItems.value.length === 0) {
    ElMessage.info('购物车已为空');
    return;
  }
  
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const goodsIds = cartItems.value.map(item => item.goodsId);
      await cartApi.deleteCartItems(goodsIds);
      cartItems.value = [];
      ElMessage.success('购物车已清空');
    } catch (error) {
      console.error('清空购物车失败:', error);
      ElMessage.error('清空失败');
    }
  }).catch(() => {
    // 取消清空
  });
}

// 去结算
function checkout() {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空');
    return;
  }
  // 跳转到支付页面
  router.push('/payment');
}

// 页面加载时获取购物车列表
onMounted(() => {
  getCartList();
});
</script>

<style scoped>
.cart {
  min-height: 100vh;
  padding-top: 80px;
  background-color: #f5f5f5;
}

.cart-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

/* 顶部标题栏 */
.cart-header {
  background-color: rgba(0, 128, 128, 0.9);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.clear-cart-btn {
  background-color: transparent;
  border: 1px solid white;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.clear-cart-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 空购物车 */
.empty-cart {
  background-color: white;
  padding: 60px;
  text-align: center;
}

.empty-cart p {
  font-size: 18px;
  margin-bottom: 30px;
  color: #666;
}

/* 加载状态 */
.loading-cart {
  background-color: white;
  padding: 60px;
  text-align: center;
}

.loading-cart p {
  font-size: 18px;
  margin-bottom: 30px;
  color: #666;
}

/* 购物车商品容器 */
.cart-items-container {
  background-color: white;
  max-height: 600px; /* 大约5个商品的高度 */
  overflow-y: auto;
  margin-bottom: 20px;
}

.cart-items {
  background-color: white;
}

.cart-item {
  display: flex;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.cart-item-image {
  width: 100px;
  height: 100px;
  margin-right: 20px;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  overflow: hidden;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-item-info {
  flex: 1;
}

.cart-item-info h3 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
}

.cart-item-specs {
  color: #666;
  font-size: 14px;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.cart-item-price {
  font-size: 18px;
  font-weight: bold;
  color: #E53E3E;
  margin: 0;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-btn {
  width: 30px;
  height: 30px;
  border: 1px solid #2E7D32;
  background-color: white;
  color: #2E7D32;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.quantity-btn:hover {
  background-color: #2E7D32;
  color: white;
}

.quantity {
  width: 40px;
  text-align: center;
  font-size: 14px;
}

.delete-btn {
  background-color: transparent;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  color: #E53E3E;
}

/* 底部总计栏 */
.cart-footer {
  background-color: #f9f9f9;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
}

.total-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-label {
  font-size: 16px;
  color: #333;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #E53E3E;
}

.checkout-btn {
  background-color: rgba(0, 128, 128, 0.9);
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.checkout-btn:hover {
  background-color: #1B5E20;
}

/* 按钮样式 */
.btn {
  padding: 10px;
  border: 1px solid #2E7D32;
  border-radius: 4px;
  background-color: white;
  color: #2E7D32;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:hover {
  background-color: #2E7D32;
  color: white;
}

.btn-primary {
  width: 200px;
  padding: 12px;
  border: none;
  border-radius: 4px;
  background-color: #2E7D32;
  color: white;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
}

.btn-primary:hover {
  background-color: #1B5E20;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-header {
    padding: 15px;
  }
  
  .cart-title {
    font-size: 20px;
  }
  
  .cart-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .cart-item-image {
    width: 100%;
    height: 200px;
    margin-right: 0;
  }
  
  .cart-item-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .cart-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .checkout-btn {
    width: 100%;
    text-align: center;
  }
}
</style>
<template>
  <div class="product-detail">
    <!-- 导航栏组件 -->
    <Navbar />

    <!-- 商品详情内容 -->
    <div class="product-detail-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="product && product.id" class="product-detail-container">
        <div class="product-image">
          <img :src="product.image" :alt="product.name" @error="handleImageError" />
        </div>
        <div class="product-info">
          <h1>{{ product.name }}</h1>
          <p class="product-price">¥{{ product.price }}</p>
          <p class="product-description">{{ product.description }}</p>
          <div class="product-stock">
            <span>库存：{{ product.stock }}件</span>
            <span>销量：{{ product.sales }}件</span>
          </div>
          <div class="product-quantity">
            <label for="quantity">数量：</label>
            <div class="quantity-control">
              <button @click="decreaseQuantity" class="quantity-btn">-</button>
              <input type="number" id="quantity" v-model="quantity" min="1" />
              <button @click="increaseQuantity" class="quantity-btn">+</button>
            </div>
          </div>
          <div class="product-actions">
            <button class="btn" @click="addToCart">加入购物车</button>
            <button class="btn" @click="goToCart">查看购物车</button>
            <button class="btn btn-primary" @click="buyNow">立即购买</button>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="商品不存在" />
      </div>
    </div>
    
    <!-- 评论模块 -->
    <CommentSection v-if="product && product.id" :goods-id="product.id" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Navbar from '../../components/Navbar.vue';
import CommentSection from '../../components/shop/CommentSection.vue';
import { findGoodById } from '../../api/goods';
import { cartApi } from '../../api/cart';
import { API_BASE } from '../../utils/apiBase';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const quantity = ref(1);
const loading = ref(false);
const product = ref(null);

// 获取商品详情
const getProductDetail = async () => {
  loading.value = true;
  try {
    const id = route.params.id;
    console.log('获取商品详情，ID:', id);
    
    // 直接使用Fetch API调用，绕过响应拦截器，查看原始响应
    const response = await fetch(`${API_BASE}/api/user/findGoodById?goodsId=${id}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    const rawData = await response.json();
    console.log('原始响应数据:', rawData);
    
    // 直接使用原始响应数据
    if (rawData.code === 1 && rawData.data) {
      const res = rawData.data;
      console.log('使用原始响应数据:', res);
      
      if (res.goodsId && res.goodsName) {
        product.value = {
          id: res.goodsId,
          name: res.goodsName,
          price: res.goodsPrice,
          description: res.goodsIntro || '暂无描述',
          image: res.goodsImage || 'https://picsum.photos/800/800?random=999',
          stock: res.stockNum || 0,
          sales: res.quantity || 1
        };
        console.log('设置后的product.value:', product.value);
      } else {
        console.error('商品详情响应缺少必要字段');
        ElMessage.error('商品不存在');
      }
    } else {
      console.error('商品详情响应为空或格式错误');
      ElMessage.error('商品不存在');
    }
  } catch (error) {
    console.error('获取商品详情失败:', error);
    ElMessage.error('获取商品详情失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

// 减少数量
function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--;
  }
}

// 增加数量
function increaseQuantity() {
  if (quantity.value < product.value.stock) {
    quantity.value++;
  }
}

// 加入购物车
async function addToCart() {
  try {
    // 先尝试获取购物车列表，检查商品是否已在购物车中
    const cartList = await cartApi.getCartList();
    const existingItem = cartList.find(item => item.goodsId === product.value.id);
    
    if (existingItem) {
      // 商品已在购物车中，更新数量
      await cartApi.updateCartItem(product.value.id, existingItem.quantity + quantity.value);
      ElMessage.success(`${product.value.name} 数量已增加`);
    } else {
      // 商品不在购物车中，添加新商品
      await cartApi.addToCart(product.value.id, quantity.value);
      ElMessage.success(`${product.value.name} 已加入购物车`);
    }
  } catch (error) {
    // 静默处理所有错误
    console.log('加入购物车操作:', error);
  }
}

// 立即购买
function buyNow() {
  // 跳转到支付页面，传递商品信息
  router.push({
    path: '/payment',
    query: {
      buyNow: 'true',
      goodsId: product.value.id,
      goodsName: encodeURIComponent(product.value.name),
      goodsPrice: product.value.price,
      goodsImage: encodeURIComponent(product.value.image),
      quantity: quantity.value
    }
  });
}

// 前往购物车
function goToCart() {
  router.push('/cart');
}

// 处理图片加载错误
function handleImageError(event) {
  event.target.src = 'https://picsum.photos/800/800?random=999';
}

// 页面加载时获取商品详情
onMounted(() => {
  getProductDetail();
});
</script>

<style scoped>
.product-detail {
  min-height: 100vh;
  padding-top: 80px;
  background-color: #f5f5f5;
}

.product-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.product-detail-container {
  display: flex;
  gap: 40px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.product-image {
  flex: 1;
  max-width: 500px;
}

.product-image img {
  width: 100%;
  height: auto;
  border-radius: 10px;
}

.product-info {
  flex: 1;
}

.product-info h1 {
  font-size: 28px;
  color: #8B4513;
  margin-bottom: 20px;
}

.product-price {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.product-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 30px;
}

.product-stock {
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
  color: #666;
}

.product-quantity {
  margin-bottom: 30px;
}

.product-quantity label {
  display: block;
  margin-bottom: 10px;
  color: #333;
  font-weight: bold;
}

.quantity-control {
  display: flex;
  align-items: center;
  width: 150px;
}

.quantity-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.quantity-control input {
  width: 70px;
  height: 40px;
  border: 1px solid #ddd;
  text-align: center;
  font-size: 16px;
}

.product-actions {
  display: flex;
  gap: 15px;
}

.btn {
  flex: 1;
  padding: 15px;
  border: 1px solid #8B4513;
  border-radius: 4px;
  background-color: white;
  color: #8B4513;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.btn:hover {
  background-color: #8B4513;
  color: white;
}

.btn-primary {
  flex: 1;
  padding: 15px;
  border: none;
  border-radius: 4px;
  background-color: #8B4513;
  color: white;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.btn-primary:hover {
  background-color: #A0522D;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail-container {
    flex-direction: column;
    padding: 20px;
  }

  .product-image {
    max-width: 100%;
  }

  .product-actions {
    flex-direction: column;
  }
}
</style>
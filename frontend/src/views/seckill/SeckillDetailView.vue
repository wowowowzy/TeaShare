<template>
  <div class="seckill-detail">
    <!-- 导航栏组件 -->
    <Navbar />

    <!-- 商品详情内容 -->
    <div class="seckill-detail-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="product && product.id" class="seckill-detail-container">
        <div class="product-image">
          <!-- 秒杀商品标识 -->
          <div class="seckill-badge">
            <span>秒杀商品</span>
          </div>
          <img :src="product.image" :alt="product.name" @error="handleImageError" />
        </div>
        <div class="product-info">
          <h1>{{ product.name }}</h1>
          <p class="product-price seckill-price">¥{{ product.price }}</p>
          <div class="product-stock seckill-stock">
            <span>剩余库存量：{{ product.stock }}件</span>
          </div>
          <div class="product-quantity">
            <label>数量：</label>
            <span class="quantity-fixed">1件</span>
          </div>
          <div class="product-actions">
            <button class="btn btn-primary" @click="buyNow" :disabled="product.stock <= 0">
              {{ product.stock <= 0 ? '已售罄' : '直接抢购' }}
            </button>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="商品不存在" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Navbar from '../../components/Navbar.vue';
import { API_BASE } from '../../utils/apiBase';
import { findGoodById } from '../../api/goods';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const product = ref(null);

// 获取商品详情
const getProductDetail = async () => {
  loading.value = true;
  try {
    const id = route.params.id;
    console.log('获取秒杀商品详情，ID:', id);
    
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
          image: res.goodsImage || 'https://picsum.photos/800/800?random=999',
          stock: res.stockNum || 0
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

// 直接抢购
async function buyNow() {
  if (!product.value || product.value.stock <= 0) return;
  
  // 库存校验
  try {
    // 再次检查库存，确保商品仍有库存
    const id = route.params.id;
    const response = await fetch(`${API_BASE}/api/user/findGoodById?goodsId=${id}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    const rawData = await response.json();
    if (rawData.code === 1 && rawData.data) {
      const currentStock = rawData.data.stockNum || 0;
      if (currentStock <= 0) {
        ElMessage.error('商品已售罄');
        return;
      }
      
      // 跳转到支付页面，携带秒杀商品信息
      router.push({
        path: '/payment',
        query: {
          seckill: 'true',
          goodsId: product.value.id,
          goodsName: product.value.name,
          goodsPrice: product.value.price,
          goodsImage: product.value.image,
          quantity: '1'
        }
      });
    } else {
      ElMessage.error('获取商品信息失败');
    }
  } catch (error) {
    console.error('库存校验失败:', error);
    ElMessage.error('网络错误，请重试');
  }
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
.seckill-detail {
  min-height: 100vh;
  padding-top: 80px;
  background-color: #f5f5f5;
}

.seckill-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.seckill-detail-container {
  display: flex;
  gap: 40px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
  border: 2px solid #E53E3E;
  position: relative;
}

/* 秒杀商品标识 */
.seckill-badge {
  position: absolute;
  top: 0;
  left: 0;
  background-color: #E53E3E;
  color: white;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  border-bottom-right-radius: 10px;
  z-index: 10;
}

.product-image {
  flex: 1;
  max-width: 500px;
  position: relative;
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

.seckill-price {
  font-size: 32px !important;
  font-weight: bold;
  color: #E53E3E !important;
  margin-bottom: 20px;
}

.seckill-stock {
  margin-bottom: 20px;
  color: #E53E3E;
  font-weight: bold;
  font-size: 18px;
}

.product-quantity {
  margin-bottom: 30px;
  display: flex;
  align-items: center;
}

.product-quantity label {
  color: #333;
  font-weight: bold;
  margin-right: 10px;
}

.quantity-fixed {
  color: #E53E3E;
  font-weight: bold;
  font-size: 16px;
}

.product-actions {
  display: flex;
  justify-content: center;
}

.btn-primary {
  width: 100%;
  max-width: 300px;
  padding: 18px;
  border: none;
  border-radius: 8px;
  background-color: #E53E3E;
  color: white;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 18px;
  font-weight: bold;
}

.btn-primary:hover {
  background-color: #C53030;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(229, 62, 62, 0.3);
}

.btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  background-color: #E53E3E;
  transform: none;
  box-shadow: none;
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
  .seckill-detail-container {
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
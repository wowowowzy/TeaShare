<template>
  <div class="seckill-goods-list">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>
    
    <!-- 秒杀商品列表 -->
    <div v-else-if="seckillGoods.length > 0" class="products-grid">
      <div class="product-item seckill-item" v-for="product in seckillGoods" :key="product.id">
        <!-- 秒杀商品标识 -->
        <div class="seckill-badge">
          <span>秒杀商品</span>
        </div>
        
        <div class="product-image">
          <img :src="product.image" :alt="product.name" @error="handleImageError" />
        </div>
        
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="product-price seckill-price">¥{{ product.price }}</p>
          <p class="product-description">{{ product.description }}</p>
          <p class="product-stock">剩余库存量：{{ product.stock }}</p>
          <div class="product-actions">
            <el-button @click="openProductInNewTab(product.id)" style="flex: 1">查看详情</el-button>
            <el-button type="primary" @click="addToCart(product)" style="flex: 1" :disabled="product.stock <= 0">
              {{ product.stock <= 0 ? '已售罄' : '加入购物车' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="暂无秒杀商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getSeckillGoods } from '../../api/goods';
import { cartApi } from '../../api/cart';
import { ElMessage } from 'element-plus';

const seckillGoods = ref([]);
const loading = ref(false);

// 获取秒杀商品列表
const loadSeckillGoods = async () => {
  loading.value = true;
  try {
    const res = await getSeckillGoods();
    console.log('秒杀商品响应:', res);
    
    // 处理响应数据
    if (res.code === 1 && res.data) {
      seckillGoods.value = res.data.map(item => ({
        id: item.goodsId,
        name: item.goodsName,
        price: item.goodsPrice,
        description: item.goodsIntro || '暂无描述',
        image: item.goodsImage || 'https://picsum.photos/800/800?random=' + Math.floor(Math.random() * 100),
        stock: item.stockNum || 0
      }));
    } else {
      seckillGoods.value = [];
    }
  } catch (error) {
    console.error('获取秒杀商品失败:', error);
    ElMessage.error('获取秒杀商品失败，请检查网络连接');
    seckillGoods.value = [];
  } finally {
    loading.value = false;
  }
};

// 在新标签页中打开商品详情
const openProductInNewTab = (productId) => {
  const url = `/seckill/${productId}`;
  window.open(url, '_blank');
};

// 加入购物车
async function addToCart(product) {
  if (product.stock <= 0) return;
  
  try {
    // 先尝试获取购物车列表，检查商品是否已在购物车中
    const cartList = await cartApi.getCartList();
    const existingItem = cartList.find(item => item.goodsId === product.id);
    
    if (existingItem) {
      // 商品已在购物车中，更新数量
      await cartApi.updateCartItem(product.id, existingItem.quantity + 1);
      ElMessage.success(`${product.name} 数量已增加`);
    } else {
      // 商品不在购物车中，添加新商品
      await cartApi.addToCart(product.id, 1);
      ElMessage.success(`${product.name} 已加入购物车`);
    }
  } catch (error) {
    // 静默处理所有错误
    console.log('加入购物车操作:', error);
  }
}

// 处理图片加载错误
function handleImageError(event) {
  event.target.src = 'https://picsum.photos/800/800?random=999';
}

// 页面加载时获取秒杀商品列表
onMounted(() => {
  loadSeckillGoods();
});
</script>

<style scoped>
.seckill-goods-list {
  margin-top: 30px;
}

/* 秒杀商品标识 */
.seckill-badge {
  position: absolute;
  top: 0;
  left: 0;
  background-color: #E53E3E;
  color: white;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: bold;
  border-bottom-right-radius: 10px;
  z-index: 10;
}

/* 秒杀商品样式 */
.seckill-item {
  border: 2px solid #E53E3E;
  position: relative;
}

.seckill-price {
  color: #E53E3E !important;
  font-size: 24px !important;
}

/* 剩余库存样式 */
.product-stock {
  color: #E53E3E;
  font-weight: bold;
  margin-bottom: 15px;
  font-size: 14px;
}

/* 商品列表样式 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 30px;
}

.product-item {
  background-color: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-item:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.product-image {
  height: 200px;
  overflow: hidden;
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-item:hover .product-image img {
  transform: scale(1.1);
}

.product-info {
  padding: 20px;
}

.product-info h3 {
  color: #8B4513;
  margin-bottom: 10px;
  font-size: 16px;
  height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  font-size: 20px;
  font-weight: bold;
  color: #E53E3E;
  margin-bottom: 10px;
}

.product-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
  height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.product-actions {
  display: flex;
  gap: 10px;
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
  .products-grid {
    grid-template-columns: 1fr;
  }
}
</style>
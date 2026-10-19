<template>
  <el-dialog
    v-model="dialogVisible"
    title="秒杀商品"
    width="90%"
    :max-width="'800px'"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    @close="handleClose"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>
    
    <!-- 秒杀商品列表 -->
    <div v-else-if="seckillGoods.length > 0" class="seckill-goods-grid">
      <div class="seckill-goods-item" v-for="product in seckillGoods" :key="product.id">
        <!-- 秒杀商品标识 -->
        <div class="seckill-badge">
          <span>秒杀商品</span>
        </div>
        
        <div class="seckill-goods-image">
          <img :src="product.image" :alt="product.name" @error="handleImageError" />
        </div>
        
        <div class="seckill-goods-info">
          <h3 class="seckill-goods-name">{{ product.name }}</h3>
          <div class="seckill-goods-price">
            <span class="seckill-price">¥{{ product.price }}</span>
            <span class="original-price">¥{{ product.originalPrice || product.price * 1.5 }}</span>
          </div>
          <p class="seckill-goods-stock">剩余库存量：{{ product.stock }}</p>
          <el-button 
            type="primary" 
            class="seckill-buy-button" 
            @click="handleBuy(product)"
            :disabled="product.stock <= 0"
          >
            {{ product.stock <= 0 ? '已售罄' : '立即抢购' }}
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="暂无秒杀商品" />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { getSeckillGoods } from '../../api/goods';
import { cartApi } from '../../api/cart';
import { ElMessage } from 'element-plus';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'close']);

const seckillGoods = ref([]);
const loading = ref(false);

// 计算属性，处理双向绑定
const dialogVisible = computed({
  get() {
    return props.visible;
  },
  set(value) {
    emit('update:visible', value);
  }
});

// 监听弹窗显示状态，当显示时加载数据
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadSeckillGoods();
  }
});

// 获取秒杀商品列表
const loadSeckillGoods = async () => {
  loading.value = true;
  try {
    const res = await getSeckillGoods();
    console.log('秒杀商品响应:', res);
    
    // 由于request.js的响应拦截器已经处理了code字段，
    // 当code === 1时，res已经是res.data
    console.log('处理后的响应数据:', res);
    
    // 直接使用res数据
    let goodsList = [];
    if (Array.isArray(res)) {
      goodsList = res;
    } else if (res.data && Array.isArray(res.data)) {
      goodsList = res.data;
    } else if (res.records && Array.isArray(res.records)) {
      goodsList = res.records;
    }
    
    seckillGoods.value = goodsList.map(item => ({
      id: item.goodsId,
      name: item.goodsName,
      price: item.goodsPrice,
      originalPrice: item.originalPrice || item.goodsPrice * 1.5, // 模拟原价
      description: item.goodsIntro || '暂无描述',
      image: item.goodsImage || 'https://picsum.photos/800/800?random=' + Math.floor(Math.random() * 100),
      stock: item.stockNum || 0
    }));
  } catch (error) {
    console.error('获取秒杀商品失败:', error);
    ElMessage.error('获取秒杀商品失败，请检查网络连接');
    seckillGoods.value = [];
  } finally {
    loading.value = false;
  }
};

// 立即抢购
async function handleBuy(product) {
  if (product.stock <= 0) return;
  
  // 关闭弹窗
  handleClose();
  
  // 直接跳转到支付页面，携带秒杀商品信息（使用特价）
  const paymentUrl = `/payment?seckill=true&goodsId=${product.id}&goodsName=${encodeURIComponent(product.name)}&goodsPrice=${product.price}&goodsImage=${encodeURIComponent(product.image)}&quantity=1`;
  window.location.href = paymentUrl;
}

// 处理图片加载错误
function handleImageError(event) {
  event.target.src = 'https://picsum.photos/800/800?random=999';
}

// 关闭弹窗
function handleClose() {
  emit('update:visible', false);
  emit('close');
}

// 页面加载时获取秒杀商品列表
onMounted(() => {
  if (props.visible) {
    loadSeckillGoods();
  }
});
</script>

<style scoped>
/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  font-size: 16px;
}

/* 秒杀商品网格 */
.seckill-goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

/* 秒杀商品项 */
.seckill-goods-item {
  background-color: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 2px solid #E53E3E;
  position: relative;
}

.seckill-goods-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
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

/* 商品图片 */
.seckill-goods-image {
  height: 180px;
  overflow: hidden;
  position: relative;
}

.seckill-goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.seckill-goods-item:hover .seckill-goods-image img {
  transform: scale(1.1);
}

/* 商品信息 */
.seckill-goods-info {
  padding: 15px;
}

.seckill-goods-name {
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

/* 价格信息 */
.seckill-goods-price {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.seckill-price {
  font-size: 24px;
  font-weight: bold;
  color: #E53E3E;
  margin-right: 10px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

/* 库存信息 */
.seckill-goods-stock {
  color: #E53E3E;
  font-weight: bold;
  margin-bottom: 15px;
  font-size: 14px;
}

/* 立即抢购按钮 */
.seckill-buy-button {
  width: 100%;
  background-color: #E53E3E !important;
  border-color: #E53E3E !important;
}

.seckill-buy-button:hover {
  background-color: #C53030 !important;
  border-color: #C53030 !important;
}

/* 滚动条样式 */
.seckill-goods-grid::-webkit-scrollbar {
  width: 6px;
}

.seckill-goods-grid::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.seckill-goods-grid::-webkit-scrollbar-thumb {
  background: #E53E3E;
  border-radius: 3px;
}

.seckill-goods-grid::-webkit-scrollbar-thumb:hover {
  background: #C53030;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .seckill-goods-grid {
    grid-template-columns: 1fr;
    max-height: 50vh;
  }
  
  .seckill-goods-image {
    height: 150px;
  }
  
  .seckill-price {
    font-size: 20px;
  }
}
</style>
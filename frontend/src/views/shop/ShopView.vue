<template>
  <div class="shop">
    <!-- 导航栏组件 -->
    <Navbar />

    <!-- 趣味闯关按钮 -->
    <div class="quiz-button-container">
      <div class="quiz-button" @click="openQuiz">
        <span class="quiz-icon">🎯</span>
        <span class="quiz-text">趣味闯关</span>
      </div>
      <p class="quiz-hint">答对三道送优惠券哦</p>
    </div>

    <!-- 秒杀商品按钮 -->
    <div class="seckill-button-container">
      <div class="seckill-button" @click="toggleSeckill">
        <span class="seckill-icon">🔥</span>
        <span class="seckill-text">秒杀商品</span>
      </div>
      <p class="seckill-hint">特价商品限量购买</p>
    </div>

    <!-- 趣味闯关组件 -->
    <QuizChallenge v-if="showQuiz" @close="closeQuiz" />
    
    <!-- 秒杀商品弹窗组件 -->
    <SeckillModal v-model:visible="showSeckill" @close="handleSeckillClose" />

    <!-- 商城内容 -->
    <div class="shop-content">
      <!-- <h1 class="section-title">茶文化商城</h1> -->
      
    <!-- 搜索框 -->
      <div class="search-box" style="text-align: center;">
        <el-input v-model="searchQuery" placeholder="请输入商品名称搜索" clearable style="width: 1000px;height: 45px;"
          @keyup.enter="handleSearch">
          <template #append>
            <el-button type="primary" @click="handleSearch">
              <el-icon>
                <Search />
              </el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <br />
      <br />
      <br />


      <!-- 轮播图 -->
      <div class="shop-banner">
        <el-carousel :interval="5000" type="card" height="300px">
          <el-carousel-item v-for="item in bannerList" :key="item.id">
            <div class="banner-item">
              <img :src="item.image" :alt="item.title" />
              <div class="banner-content">
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      
      <!-- 筛选和搜索区域 -->
      <div class="filter-section">

        
        <!-- 购物车按钮 -->
        <div class="cart-button">
          <el-button type="success" @click="goToCart">
            <el-icon><ShoppingCart /></el-icon>
            购物车
          </el-button>
        </div>
        
        <!-- 分类筛选 -->
        <div class="category-filter">
          <el-select
            v-model="selectedCategory"
            placeholder="商品分类"
            clearable
            style="width: 200px"
            @change="handleCategoryChange"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </div>
        
        <!-- 价格筛选 -->
        <div class="price-filter">
          <el-slider
            v-model="priceRange"
            range
            :min="0"
            :max="2000"
            :step="50"
            @change="handlePriceChange"
          />
          <div class="price-input-container">
            <el-input-number
              v-model="priceRange[0]"
              :min="0"
              :max="priceRange[1]"
              :step="50"
              @change="handlePriceInputChange"
              size="small"
              style="width: 100px; margin-right: 10px;"
            />
            <span>至</span>
            <el-input-number
              v-model="priceRange[1]"
              :min="priceRange[0]"
              :max="2000"
              :step="50"
              @change="handlePriceInputChange"
              size="small"
              style="width: 100px; margin-left: 10px;"
            />
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>
      
      <!-- 商品列表 -->
      <div v-else class="products-grid">
        <div class="product-item" v-for="product in products" :key="product.id">
          <div class="product-image">
            <img :src="product.image" :alt="product.name" @error="handleImageError" />
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="product-price">¥{{ product.price }}</p>
            <p class="product-description">销售数量：{{ product.salse }}</p>
            <div class="product-actions">
              <el-button @click="openProductInNewTab(product.id)" style="flex: 1">查看详情</el-button>
              <el-button type="primary" @click="addToCart(product)" style="flex: 1">加入购物车</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && products.length === 0" class="empty-state">
        <el-empty description="暂无商品数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Navbar from '../../components/Navbar.vue';
import QuizChallenge from '../../components/shop/QuizChallenge.vue';
import SeckillModal from '../../components/shop/SeckillModal.vue';
import { findAllGoods, findGoodById } from '../../api/goods';
import { cartApi } from '../../api/cart';
import { API_BASE } from '../../utils/apiBase';
import { ElMessage, ElCarousel, ElCarouselItem } from 'element-plus';
import { Search, ShoppingCart } from '@element-plus/icons-vue';


// 防抖函数
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}


// 轮播图数据
const bannerList = ref([
  {
    id: 1,
    title: '限时优惠',
    description: '欢迎来到茗享',
    image: '/assest/轮播图1.png'
  },
  {
    id: 2,
    title: '新品上市',
    description: '2026年新茶已到货',
    image: '/assest/轮播图2.png'
  },
  {
    id: 3,
    title: '会员专享',
    description: '优惠卷满200减50',
    image: '/assest/轮播图3.png'
  }
]);


const router = useRouter();

const showQuiz = ref(false);
const showSeckill = ref(false);

const openQuiz = () => {
  showQuiz.value = true;
};

const closeQuiz = () => {
  showQuiz.value = false;
};

const toggleSeckill = () => {
  showSeckill.value = !showSeckill.value;
};

const handleSeckillClose = () => {
  showSeckill.value = false;
};

// 商品数据
const products = ref([]);
const loading = ref(false);



// 搜索和筛选参数
const searchQuery = ref('');
const selectedCategory = ref('');
const priceRange = ref([0, 2000]);

// 商品分类（模拟数据，实际应从API获取）
const categories = ref([
  { id: 1, name: '绿茶' },
  { id: 2, name: '红茶' },
  { id: 3, name: '乌龙茶' },
  { id: 4, name: '白茶' },
  { id: 5, name: '普洱茶' },
  { id: 6, name: '特种茶' }
]);



// 获取商品列表
const getGoodsList = async () => {
  if (loading.value) return; // 避免重复请求
  loading.value = true;
  try {
    const res = await findAllGoods({
      page: 1,
      pageSize: 1000, // 一次性获取足够多的商品
      goodsName: searchQuery.value,
      categoryId: selectedCategory.value
    });
    console.log('商品列表完整响应:', res);
    
    // 由于request.js的响应拦截器已经处理了code字段，
    // 当code === 1时，res已经是res.data
    console.log('处理后的响应数据:', res);
    
    // 直接访问res.records，而不是res.data.records
    let goodsList = res.records || [];
    
    // 价格筛选
    goodsList = goodsList.filter(item => {
      const price = parseFloat(item.goodsPrice);
      return price >= priceRange.value[0] && price <= priceRange.value[1];
    });
    
    console.log('解析后的goodsList:', goodsList);
    
    products.value = goodsList.map(item => ({
      id: item.goodsId,
      name: item.goodsName,
      price: item.goodsPrice,
      salse: item.quantity,
      image: item.goodsImage || 'https://picsum.photos/800/800?random=' + Math.floor(Math.random() * 100)
    }));
  } catch (error) {
    console.error('获取商品列表失败:', error);
    ElMessage.error('获取商品列表失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

// 使用防抖包装getGoodsList函数
const debouncedGetGoodsList = debounce(getGoodsList, 300);

// 在新标签页中打开商品详情
const openProductInNewTab = (productId) => {
  const url = `/product/${productId}`;
  window.open(url, '_blank');
};

// 打开商品详情弹窗（保留但不再使用）
const openProductDetail = async (productId) => {
  detailLoading.value = true;
  quantity.value = 1;
  try {
    // 直接使用Fetch API调用，绕过响应拦截器，查看原始响应
    const response = await fetch(`${API_BASE}/api/user/findGoodById?goodsId=${productId}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    const rawData = await response.json();
    console.log('商品详情原始响应数据:', rawData);
    
    // 直接使用原始响应数据
    if (rawData.code === 1 && rawData.data) {
      const res = rawData.data;
      console.log('使用原始响应数据:', res);
      
      if (res.goodsId && res.goodsName) {
        currentProduct.value = {
          id: res.goodsId,
          name: res.goodsName,
          price: res.goodsPrice,
          salse: res.quantity,
          image: res.goodsImage || 'https://picsum.photos/800/800?random=999',
          stock: res.stockNum || 0
        };
        console.log('设置后的currentProduct.value:', currentProduct.value);
        showDetailDialog.value = true;
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
    detailLoading.value = false;
  }
};

// 加入购物车
async function addToCart(product) {
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

// 前往购物车
function goToCart() {
  router.push('/cart');
}

// 搜索商品
function handleSearch() {
  debouncedGetGoodsList();
}

// 分类变化
function handleCategoryChange() {
  debouncedGetGoodsList();
}

// 价格范围变化（拖动滑块）
function handlePriceChange() {
  debouncedGetGoodsList();
}

// 价格输入变化（直接输入）
function handlePriceInputChange() {
  // 确保最小值不大于最大值
  if (priceRange.value[0] > priceRange.value[1]) {
    priceRange.value[1] = priceRange.value[0];
  }
  debouncedGetGoodsList();
}

// 处理图片加载错误
function handleImageError(event) {
  event.target.src = 'https://picsum.photos/800/800?random=999';
}

// 页面加载时获取商品列表
onMounted(() => {
  getGoodsList();
});
</script>

<style scoped>
.shop {
  min-height: 100vh;
  padding-top: 80px;
  background-color: #f5f5f5;
  position: relative;
}

.quiz-button-container {
  position: fixed;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quiz-button {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(238, 90, 90, 0.4);
  transition: all 0.3s ease;
}

.quiz-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(238, 90, 90, 0.6);
}

.quiz-icon {
  font-size: 28px;
}

.quiz-text {
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  margin-top: 2px;
}

.quiz-hint {
  margin-top: 10px;
  color: #ff6b6b;
  font-size: 12px;
  font-weight: bold;
  background-color: #fff;
  padding: 4px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
}

/* 秒杀商品按钮样式 */
.seckill-button-container {
  position: fixed;
  left: 30px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.seckill-button {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(238, 90, 90, 0.4);
  transition: all 0.3s ease;
}

.seckill-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(238, 90, 90, 0.6);
}

.seckill-icon {
  font-size: 28px;
}

.seckill-text {
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  margin-top: 2px;
}

.seckill-hint {
  margin-top: 10px;
  color: #ff6b6b;
  font-size: 12px;
  font-weight: bold;
  background-color: #fff;
  padding: 4px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
}

/* 秒杀商品区域样式 */
.seckill-section {
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 20px;
}

.seckill-title {
  font-size: 24px;
  color: #E53E3E;
  margin-bottom: 20px;
  text-align: center;
  font-weight: bold;
}

.shop-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.section-title {
  font-size: 32px;
  color: #8B4513;
  margin-bottom: 40px;
  text-align: center;
}

/* 轮播图 */
.shop-banner {
  margin-bottom: 30px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.banner-item {
  position: relative;
  height: 300px;
  overflow: hidden;
}

.banner-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: white;
  padding: 30px;
}

.banner-content h3 {
  margin: 0 0 15px 0;
  font-size: 24px;
  font-weight: bold;
}

.banner-content p {
  margin: 0;
  font-size: 16px;
}

/* 筛选和搜索区域 */
.filter-section {
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.search-box {
  flex: 1;
  min-width: 250px;
}

.category-filter {
  min-width: 200px;
}

.cart-button {
  min-width: 120px;
}

.price-filter {
  flex: 1;
  min-width: 300px;
}

.price-input-container {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #666;
  font-size: 14px;
}

/* 商品列表 */
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
  margin-bottom: 20px;
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

.btn {
  flex: 1;
  padding: 10px;
  border: 1px solid #8B4513;
  border-radius: 4px;
  background-color: white;
  color: #8B4513;
  text-align: center;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.btn:hover {
  background-color: #8B4513;
  color: white;
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
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box,
  .category-filter,
  .price-filter {
    width: 100%;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .product-detail-container {
    flex-direction: column;
    padding: 10px 0;
  }
  
  .product-detail-container .product-image {
    max-width: 100%;
  }
  
  .product-detail-container .product-actions {
    flex-direction: column;
  }
}
</style>
<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2 class="page-title">仪表盘</h2>
      <div class="admin-info">
          <el-dropdown class="admin-actions">
            <i class="el-icon-more"></i>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item>修改密码</el-dropdown-item>
                <el-dropdown-item>系统设置</el-dropdown-item>
                <el-dropdown-item>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
      </div>
    </div>
    
    <!-- 数据卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon product-icon">
          <i class="el-icon-goods"></i>
        </div>
        <div class="stat-content">
          <h3>总产品数</h3>
          <p class="stat-value">{{ totalProducts }}</p>
          <p class="stat-change positive">+12% 较上月</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon order-icon">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-content">
          <h3>订单量</h3>
          <p class="stat-value">{{ totalOrders }}</p>
          <p class="stat-change positive">+8% 较上月</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon user-icon">
          <i class="el-icon-user"></i>
        </div>
        <div class="stat-content">
          <h3>用户数量</h3>
          <p class="stat-value">{{ totalUsers }}</p>
          <p class="stat-change positive">+15% 较上月</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon revenue-icon">
          <i class="el-icon-money"></i>
        </div>
        <div class="stat-content">
          <h3>总销售额</h3>
          <p class="stat-value">¥{{ totalRevenue }}</p>
          <p class="stat-change positive">+20% 较上月</p>
        </div>
      </div>
    </div>
    
    <!-- 年份选择器 -->
    <div class="year-selector">
      <el-select v-model="selectedYear" @change="handleYearChange" placeholder="选择年份">
        <el-option v-for="year in years" :key="year" :label="year + '年'" :value="year" />
      </el-select>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 年度月度订单销售数量图表 -->
      <div class="chart-card">
        <h3>年度月度订单销售数量</h3>
        <div class="chart-wrapper">
          <div v-if="loading" class="loading-overlay">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div ref="orderChartRef" class="chart"></div>
        </div>
      </div>
      
      <!-- 年度月度客户注册数量图表 -->
      <div class="chart-card">
        <h3>年度月度客户注册数量</h3>
        <div class="chart-wrapper">
          <div v-if="loading" class="loading-overlay">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div ref="userChartRef" class="chart"></div>
        </div>
      </div>
      
      <!-- 商品种类购买数量分布图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>商品种类购买数量分布</h3>
          <div class="chart-actions">
            <el-select v-model="sortBy" @change="initProductChart" placeholder="排序方式">
              <el-option label="按数量降序" value="desc" />
              <el-option label="按数量升序" value="asc" />
              <el-option label="按名称排序" value="name" />
            </el-select>
          </div>
        </div>
        <div class="chart-wrapper">
          <div v-if="loading" class="loading-overlay">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div ref="productChartRef" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { getDashboardData, getOrderCountStatistics, getCustomerCountStatistics, getGoodsCategoryCountStatistics } from '../../api/dashboard';

// 数据
const totalProducts = ref(128);
const totalOrders = ref(356);
const totalUsers = ref(2158);
const totalRevenue = ref(128500);
const loading = ref(true);

// 年份选择相关
const years = [2023, 2024, 2025, 2026, 2027, 2028];
const selectedYear = ref(2026);

// 排序方式
const sortBy = ref('desc');

// 图表数据
const orderData = ref([
  { year: 2023, month: 1, order_count: 1250 },
  { year: 2023, month: 2, order_count: 1420 },
  { year: 2023, month: 3, order_count: 1350 },
  { year: 2023, month: 4, order_count: 1600 },
  { year: 2023, month: 5, order_count: 1750 },
  { year: 2023, month: 6, order_count: 1900 },
  { year: 2023, month: 7, order_count: 2100 },
  { year: 2023, month: 8, order_count: 2300 },
  { year: 2023, month: 9, order_count: 2150 },
  { year: 2023, month: 10, order_count: 2400 },
  { year: 2023, month: 11, order_count: 2600 },
  { year: 2023, month: 12, order_count: 2800 }
]);
const userData = ref([
  { year: 2023, month: 1, user_registrations: 850 },
  { year: 2023, month: 2, user_registrations: 920 },
  { year: 2023, month: 3, user_registrations: 880 },
  { year: 2023, month: 4, user_registrations: 1050 },
  { year: 2023, month: 5, user_registrations: 1120 },
  { year: 2023, month: 6, user_registrations: 1200 },
  { year: 2023, month: 7, user_registrations: 1350 },
  { year: 2023, month: 8, user_registrations: 1480 },
  { year: 2023, month: 9, user_registrations: 1390 },
  { year: 2023, month: 10, user_registrations: 1520 },
  { year: 2023, month: 11, user_registrations: 1650 },
  { year: 2023, month: 12, user_registrations: 1780 }
]);
const productData = ref([
  { product_category: '电子产品', purchase_count: 5200 },
  { product_category: '服装', purchase_count: 3800 },
  { product_category: '食品', purchase_count: 4500 },
  { product_category: '家居用品', purchase_count: 2800 },
  { product_category: '化妆品', purchase_count: 3200 }
]);

// 图表引用
const orderChartRef = ref(null);
const userChartRef = ref(null);
const productChartRef = ref(null);

// 图表实例
let orderChart = null;
let userChart = null;
let productChart = null;

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    // 获取仪表盘数据
    const dashboardData = await getDashboardData();
    totalProducts.value = dashboardData.goodsTotal;
    totalOrders.value = dashboardData.orderTotal;
    totalUsers.value = dashboardData.userTotal;
    totalRevenue.value = dashboardData.priceTotal;
    
    // 获取订单数据
    await fetchOrderData();
    // 获取用户数据
    await fetchUserData();
    // 获取商品数据
    await fetchProductData();
    
    // 确保DOM元素加载后再初始化图表
    await nextTick();
    initOrderChart();
    initUserChart();
    initProductChart();
  } catch (error) {
    ElMessage.error('获取数据失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 获取年度月度订单销售数量数据
const fetchOrderData = async () => {
  try {
    const orderList = await getOrderCountStatistics();
    // 转换数据格式以适应图表需求
    orderData.value = orderList.map(item => ({
      year: item.year,
      month: item.month,
      order_count: item.num
    }));
  } catch (error) {
    ElMessage.error('获取订单数据失败：' + error.message);
  }
};

// 获取年度月度客户注册数量数据
const fetchUserData = async () => {
  try {
    const userList = await getCustomerCountStatistics();
    // 转换数据格式以适应图表需求
    userData.value = userList.map(item => ({
      year: item.year,
      month: item.month,
      user_registrations: item.num
    }));
  } catch (error) {
    ElMessage.error('获取用户数据失败：' + error.message);
  }
};

// 商品类别ID到名称的映射
const categoryMap = {
  1: '绿茶',
  2: '红茶',
  3: '乌龙茶',
  4: '白茶',
  5: '黑茶',
  6: '特级茶'
};

// 获取商品种类购买数量数据
const fetchProductData = async () => {
  try {
    const productList = await getGoodsCategoryCountStatistics();
    // 转换数据格式以适应图表需求，使用类别名称映射
    productData.value = productList.map(item => ({
      product_category: categoryMap[item.categoryId] || `类别${item.categoryId}`,
      purchase_count: item.orderCount
    }));
  } catch (error) {
    ElMessage.error('获取商品数据失败：' + error.message);
  }
};

// 年份变化处理
const handleYearChange = async () => {
  loading.value = true;
  try {
    // 重新获取数据
    await fetchOrderData();
    await fetchUserData();
    
    // 确保DOM元素加载后再初始化图表
    await nextTick();
    initOrderChart();
    initUserChart();
  } catch (error) {
    ElMessage.error('更新图表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 初始化订单趋势图
const initOrderChart = () => {
  if (orderChartRef.value) {
    orderChart = echarts.init(orderChartRef.value);
    
    // 处理数据
    const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
    const orderCounts = new Array(12).fill(0);
    
    orderData.value.forEach(item => {
      if (item.year === selectedYear.value) {
        orderCounts[item.month - 1] = item.order_count;
      }
    });
    
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#4CAF50'
          }
        },
        formatter: function(params) {
          return params[0].name + '<br/>订单数量: ' + params[0].value;
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: months
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '订单数量',
          // 自动计算Y轴范围，确保数据能够正确显示
          min: function(value) {
            return Math.max(0, Math.floor(value.min * 0.9));
          },
          max: function(value) {
            return Math.ceil(value.max * 1.1);
          }
        }
      ],
      series: [
        {
          name: '订单量',
          type: 'bar',
          data: orderCounts,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#4CAF50' },
              { offset: 1, color: '#8BC34A' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#8BC34A' },
                { offset: 1, color: '#CDDC39' }
              ])
            }
          }
        }
      ]
    };
    orderChart.setOption(option);
  }
};

// 初始化用户增长图
const initUserChart = () => {
  if (userChartRef.value) {
    userChart = echarts.init(userChartRef.value);
    
    // 处理数据
    const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
    const userRegistrations = new Array(12).fill(0);
    
    userData.value.forEach(item => {
      if (item.year === selectedYear.value) {
        userRegistrations[item.month - 1] = item.user_registrations;
      }
    });
    
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#8BC34A'
          }
        },
        formatter: function(params) {
          return params[0].name + '<br/>注册数量: ' + params[0].value;
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: months
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '注册数量',
          // 自动计算Y轴范围，确保数据能够正确显示
          min: function(value) {
            return Math.max(0, Math.floor(value.min * 0.9));
          },
          max: function(value) {
            return Math.ceil(value.max * 1.1);
          }
        }
      ],
      series: [
        {
          name: '用户注册',
          type: 'line',
          smooth: true,
          areaStyle: {
            opacity: 0.3,
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(139, 195, 74, 0.6)' },
              { offset: 1, color: 'rgba(139, 195, 74, 0.1)' }
            ])
          },
          lineStyle: {
            color: '#8BC34A'
          },
          itemStyle: {
            color: '#8BC34A'
          },
          emphasis: {
            focus: 'series'
          },
          data: userRegistrations
        }
      ]
    };
    userChart.setOption(option);
  }
};

// 初始化产品分类图
const initProductChart = () => {
  if (productChartRef.value) {
    productChart = echarts.init(productChartRef.value);
    
    // 处理数据
    let sortedData = [...productData.value];
    
    // 根据排序方式排序
    switch (sortBy.value) {
      case 'desc':
        sortedData.sort((a, b) => b.purchase_count - a.purchase_count);
        break;
      case 'asc':
        sortedData.sort((a, b) => a.purchase_count - b.purchase_count);
        break;
      case 'name':
        sortedData.sort((a, b) => a.product_category.localeCompare(b.product_category));
        break;
    }
    
    const chartData = sortedData.map(item => ({
      name: item.product_category,
      value: item.purchase_count
    }));
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          return params.name + '<br/>购买数量: ' + params.value + '<br/>占比: ' + params.percent + '%';
        }
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        type: 'scroll'
      },
      series: [
        {
          name: '商品种类',
          type: 'pie',
          radius: '60%',
          center: ['60%', '50%'],
          data: chartData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            show: true,
            formatter: '{b}'
          }
        }
      ],
      color: ['#4CAF50', '#8BC34A', '#CDDC39', '#FFC107', '#FF9800', '#FF5722', '#795548', '#9E9E9E']
    };
    productChart.setOption(option);
  }
};

// 响应式调整
const handleResize = () => {
  orderChart?.resize();
  userChart?.resize();
  productChart?.resize();
};

onMounted(() => {
  fetchData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  orderChart?.dispose();
  userChart?.dispose();
  productChart?.dispose();
});
</script>

<style scoped>
/* 加载动画 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  z-index: 10;
}

.loading-overlay .el-icon {
  font-size: 32px;
  color: #4CAF50;
}

.chart-wrapper {
  position: relative;
  height: 300px;
}

/* 空状态样式 */
.empty-state {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

/* 年份选择器样式 */
.year-selector {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.year-selector .el-select {
  width: 120px;
}

/* 图表标题和操作按钮样式 */
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-header h3::before {
  content: "";
  width: 3px;
  height: 16px;
  background-color: #4CAF50;
  border-radius: 1.5px;
}

.chart-actions {
  display: flex;
  gap: 10px;
}

.chart-actions .el-select {
  width: 150px;
}
</style>

<style scoped>
.dashboard {
  padding: 20px 0;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title::before {
  content: "";
  width: 4px;
  height: 24px;
  background-color: #4CAF50;
  border-radius: 2px;
}

/* 管理员信息卡片 */
.admin-info {
  flex-shrink: 0;
}

.admin-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 300px;
  border-left: 4px solid #4CAF50;
}

.admin-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #4CAF50;
  flex-shrink: 0;
}

.admin-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-details {
  flex: 1;
}

.admin-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #333;
}

.admin-position {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
}

.admin-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.admin-stat-item {
  font-size: 12px;
  color: #888;
  display: flex;
  align-items: center;
  gap: 5px;
}

.admin-stat-item i {
  color: #4CAF50;
  font-size: 12px;
}

.admin-actions {
  flex-shrink: 0;
  cursor: pointer;
  color: #666;
  font-size: 16px;
  padding: 5px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.admin-actions:hover {
  background-color: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

/* 数据卡片样式 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  border: 1px solid rgba(76, 175, 80, 0.2);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  border-color: #4CAF50;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: #fff;
}

.product-icon {
  background-color: #4CAF50;
}

.order-icon {
  background-color: #8BC34A;
}

.user-icon {
  background-color: #CDDC39;
}

.revenue-icon {
  background-color: #FFC107;
}

.stat-content h3 {
  font-size: 14px;
  color: #666;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.stat-change {
  font-size: 12px;
  margin: 0;
}

.stat-change.positive {
  color: #4CAF50;
}

.stat-change.negative {
  color: #e74c3c;
}

/* 图表容器样式 */
.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(76, 175, 80, 0.2);
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-card h3::before {
  content: "";
  width: 3px;
  height: 16px;
  background-color: #4CAF50;
  border-radius: 1.5px;
}

.chart-wrapper {
  height: 300px;
}

.chart {
  width: 100%;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .admin-card {
    min-width: 100%;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .chart-wrapper {
    height: 250px;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .chart-actions {
    width: 100%;
  }
  
  .chart-actions .el-select {
    width: 100%;
  }
}
</style>
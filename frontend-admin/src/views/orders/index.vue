<template>
  <div class="order-management">
    <h2 class="page-title">订单管理</h2>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-input
        v-model="searchQuery"
        placeholder="搜索订单号"
        prefix-icon="el-icon-search"
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="statusFilter" placeholder="选择订单状态" class="filter-select">
        <el-option label="全部" value="" />
        <el-option label="未审核" value="0" />
        <el-option label="已审核" value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>
    
    <!-- 订单表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      <el-table
        v-loading="loading"
        :data="paginatedOrders"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="orderId" label="订单ID" />
        <el-table-column prop="createTime" label="下单时间">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总金额">
          <template #default="scope">
            ¥{{ scope.row.totalPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewOrder(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="() => handleApprovalOrder(scope.row.orderId)" v-if="scope.row.status === 0">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalOrders"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="showOrderDetail"
      title="订单详情"
      width="800px"
    >
      <div class="order-detail" v-if="selectedOrder">
        <div class="order-header">
          <div class="order-info">
            <h3>订单信息</h3>
            <div class="info-item">
              <label>订单ID：</label>
              <span>{{ selectedOrder.orderId }}</span>
            </div>
            <div class="info-item">
              <label>下单时间：</label>
              <span>{{ formatDate(selectedOrder.createTime) }}</span>
            </div>
            <div class="info-item">
              <label>订单状态：</label>
              <el-tag :type="getStatusType(selectedOrder.status)">
                {{ getStatusText(selectedOrder.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <label>总金额：</label>
              <span class="total-amount">¥{{ selectedOrder.totalPrice.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        
        <div class="order-items">
          <h3>订单商品</h3>
          <el-table
            :data="selectedOrder.orderDTOList"
            style="width: 100%"
            border
          >
            <el-table-column label="商品图片" width="100">
              <template #default="scope">
                <el-image
                  :src="scope.row.goodsImage"
                  fit="cover"
                  style="width: 80px; height: 80px"
                />
              </template>
            </el-table-column>
            <el-table-column prop="goodsName" label="商品名称" />
            <el-table-column prop="goodsPrice" label="单价" width="100">
              <template #default="scope">
                ¥{{ scope.row.goodsPrice.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="120">
              <template #default="scope">
                ¥{{ (scope.row.goodsPrice * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { getAllOrders, approvalOrder } from '../../api/aorder';

// 订单数据
const orders = ref([]);
const loading = ref(true);
const isFetching = ref(false); // 防止重复调用的标志
const isApproving = ref(false); // 防止重复审批的标志

// 搜索和筛选
const searchQuery = ref('');
const statusFilter = ref('');
const currentPage = ref(1);
const pageSize = ref(10);

// 订单详情
const showOrderDetail = ref(false);
const selectedOrder = ref(null);

// 过滤订单
const filteredOrders = computed(() => {
  let result = orders.value;
  
  // 按订单号搜索
  if (searchQuery.value) {
    const query = searchQuery.value.trim();
    result = result.filter(order => 
      order.orderId.includes(query)
    );
  }
  
  // 按状态筛选
  if (statusFilter.value !== '') {
    const status = parseInt(statusFilter.value);
    result = result.filter(order => order.status === status);
  }
  
  return result;
});

// 总订单数
const totalOrders = computed(() => {
  return filteredOrders.value.length;
});

// 分页后的订单
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredOrders.value.slice(start, end);
});

// 获取订单数据
const fetchOrders = async () => {
  // 防止重复调用
  if (isFetching.value) {
    console.log('订单数据正在获取中，避免重复调用');
    return;
  }
  
  isFetching.value = true;
  loading.value = true;
  try {
    const response = await getAllOrders();
    console.log('========== 获取订单数据调试 ==========');
    console.log('获取到的原始订单数据:', response);
    console.log('原始数据类型:', typeof response);
    
    if (Array.isArray(response)) {
      console.log('订单数量:', response.length);
      
      // 直接使用后端返回的orderId，不做转换
      orders.value = response.map((order, index) => {
        console.log(`订单 ${index} 后端返回的orderId:`, order.orderId);
        console.log(`订单 ${index} orderId类型:`, typeof order.orderId);
        
        return {
          ...order
          // 不再转换orderId，直接使用后端返回的值
        };
      });
      
      console.log('处理后的订单数据:', orders.value);
      console.log('第一个订单ID:', orders.value[0]?.orderId);
      console.log('第一个订单ID类型:', typeof orders.value[0]?.orderId);
    } else {
      console.error('响应数据不是数组:', response);
      orders.value = [];
    }
    console.log('=====================================');
  } catch (error) {
    console.error('获取订单数据失败:', error);
    ElMessage.error('获取订单数据失败：' + error.message);
    orders.value = [];
  } finally {
    loading.value = false;
    isFetching.value = false;
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning';
    case 1: return 'success';
    default: return 'default';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '未审核';
    case 1: return '已审核';
    default: return '未知';
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  statusFilter.value = '';
  currentPage.value = 1;
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
};

// 查看订单详情
const viewOrder = (order) => {
  selectedOrder.value = { ...order };
  showOrderDetail.value = true;
};

// 审批订单
const handleApprovalOrder = (orderId) => {
  // 防止重复审批
  if (isApproving.value) {
    console.log('订单正在审批中，避免重复操作');
    ElMessage.warning('订单正在审批中，请稍候');
    return;
  }
  
  console.log('========== 审批订单调试信息 ==========');
  console.log('直接接收的订单ID:', orderId);
  console.log('订单ID类型:', typeof orderId);
  console.log('订单ID长度:', orderId?.length);
  
  // 检查orders数组中是否存在这个订单
  const orderIndex = orders.value.findIndex(o => o.orderId === orderId);
  console.log('订单在orders数组中的索引:', orderIndex);
  if (orderIndex >= 0) {
    console.log('orders数组中对应订单的orderId:', orders.value[orderIndex].orderId);
  }
  
  console.log('=====================================');
  
  ElMessageBox.confirm('确定要审批这个订单吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    isApproving.value = true;
    try {
      console.log('调用审批接口的订单ID:', orderId);
      console.log('调用审批接口的订单ID类型:', typeof orderId);
      
      // 再次检查订单ID
      console.log('再次检查订单ID:', orderId);
      
      await approvalOrder(orderId);
      ElMessage.success('订单审批成功');
      
      // 重新获取订单数据，确保使用最新数据
      await fetchOrders();
    } catch (error) {
      console.error('审批订单失败:', error);
      ElMessage.error('审批订单失败：' + error.message);
    } finally {
      isApproving.value = false;
    }
  }).catch(() => {
    // 取消审批
  });
};

onMounted(() => {
  fetchOrders();
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

.table-container {
  position: relative;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 20px;
}
</style>

<style scoped>
.order-management {
  padding: 20px 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 30px;
  color: #333;
}

/* 搜索和筛选区域 */
.search-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.filter-select {
  width: 150px;
}

/* 表格容器 */
.table-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 订单详情 */
.order-detail {
  padding: 20px 0;
}

.order-header {
  margin-bottom: 30px;
}

.order-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #333;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.info-item label {
  width: 100px;
  font-weight: 600;
  color: #666;
}

.info-item span {
  flex: 1;
  color: #333;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #e74c3c;
}

.order-items h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .filter-select {
    width: 100%;
  }
  
  .table-container {
    overflow-x: auto;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .info-item label {
    width: 100%;
    margin-bottom: 5px;
  }
}
</style>
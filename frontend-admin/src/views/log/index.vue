<template>
  <div class="activity-log">
    <h2 class="page-title">活动日志</h2>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-input
        v-model="searchQuery"
        placeholder="搜索操作内容"
        prefix-icon="el-icon-search"
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="date-picker"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>
    
    <!-- 日志表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      <el-table
        v-loading="loading"
        :data="filteredLogs"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="日志ID" width="100" />
        <el-table-column prop="operName" label="管理员" width="150" />
        <el-table-column prop="operType" label="操作" />
        <el-table-column prop="operModule" label="目标" width="150" />
        <el-table-column prop="operTime" label="操作时间" width="200">
          <template #default="scope">
            {{ formatDate(scope.row.operTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status ? 'success' : 'danger'">
              {{ scope.row.status ? '成功' : '失败' }}
            </el-tag>
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
        :total="totalLogs"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { getLog } from '../../api/alog';

// 日志数据
const logs = ref([]);
const loading = ref(true);

// 搜索和筛选
const searchQuery = ref('');
const dateRange = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalLogs = ref(0);

// 过滤日志
const filteredLogs = computed(() => {
  let result = logs.value;
  
  // 搜索过滤
  if (searchQuery.value) {
    result = result.filter(log => 
      log.operType.includes(searchQuery.value) || 
      log.operModule.includes(searchQuery.value) ||
      log.operName.includes(searchQuery.value)
    );
  }
  
  // 日期范围过滤
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    const endDate = new Date(dateRange.value[1]);
    endDate.setHours(23, 59, 59, 999);
    
    result = result.filter(log => {
      const logDate = new Date(log.operTime);
      return logDate >= startDate && logDate <= endDate;
    });
  }
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return result.slice(start, end);
});

// 获取日志数据
const fetchLogs = async () => {
  loading.value = true;
  try {
    const response = await getLog();
    logs.value = response || [];
    totalLogs.value = logs.value.length;
  } catch (error) {
    ElMessage.error('获取日志数据失败：' + error.message);
    logs.value = [];
    totalLogs.value = 0;
  } finally {
    loading.value = false;
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  let filtered = logs.value;
  
  if (searchQuery.value) {
    filtered = filtered.filter(log => 
      log.operType.includes(searchQuery.value) || 
      log.operModule.includes(searchQuery.value) ||
      log.operName.includes(searchQuery.value)
    );
  }
  
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    const endDate = new Date(dateRange.value[1]);
    endDate.setHours(23, 59, 59, 999);
    
    filtered = filtered.filter(log => {
      const logDate = new Date(log.operTime);
      return logDate >= startDate && logDate <= endDate;
    });
  }
  
  totalLogs.value = filtered.length;
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  dateRange.value = [];
  currentPage.value = 1;
  totalLogs.value = logs.value.length;
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
};

onMounted(() => {
  fetchLogs();
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
.activity-log {
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
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.date-picker {
  width: 250px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .search-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .date-picker {
    width: 100%;
  }
  
  .table-container {
    overflow-x: auto;
  }
}
</style>
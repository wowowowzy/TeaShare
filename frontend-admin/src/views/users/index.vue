<template>
  <div class="user-management">
    <h2 class="page-title">用户管理</h2>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-input
        v-model="searchQuery"
        placeholder="搜索用户名"
        prefix-icon="el-icon-search"
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>
    
    <!-- 用户表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      <el-table
        v-loading="loading"
        :data="users"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="是否被禁用" width="120">
          <template #default="scope">
            {{ scope.row.status ? '否' : '是' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-switch
              :model-value="!scope.row.status"
              :loading="scope.row.isLoading"
              active-color="#F56C6C"
              inactive-color="#67C23A"
              :active-text="'禁用'"
              :inactive-text="'正常'"
              inline-prompt
              @change="(value) => handleStatusChange(scope.row, !value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewUser(scope.row)">查看</el-button>
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
        :total="totalUsers"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 用户详情弹窗 -->
    <el-dialog
      v-model="showUserDetail"
      title="用户详情"
      width="500px"
    >
      <div class="user-detail" v-if="selectedUser">
        <div class="detail-item">
          <label>用户ID：</label>
          <span>{{ selectedUser.id }}</span>
        </div>
        <div class="detail-item">
          <label>用户名：</label>
          <span>{{ selectedUser.username }}</span>
        </div>
        <div class="detail-item">
          <label>手机号：</label>
          <span>{{ selectedUser.phone }}</span>
        </div>
        <div class="detail-item">
          <label>注册时间：</label>
          <span>{{ formatDate(selectedUser.createTime) }}</span>
        </div>
        <div class="detail-item">
          <label>最后更新时间：</label>
          <span>{{ formatDate(selectedUser.updateTime) }}</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <span>{{ selectedUser.status ? '正常' : '禁用' }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { getUserShow, banUser, unbanUser } from '../../api/user';

// 用户数据
const users = ref([]);
const loading = ref(true);
const isLoadingData = ref(false);

// 搜索和分页
const searchQuery = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const totalUsers = ref(0);

// 用户详情
const showUserDetail = ref(false);
const selectedUser = ref(null);

// 获取用户数据
const fetchUsers = async () => {
  loading.value = true;
  isLoadingData.value = true;
  try {
    const response = await getUserShow(currentPage.value, pageSize.value, searchQuery.value);
    users.value = response.records || [];
    totalUsers.value = response.total || 0;
  } catch (error) {
    ElMessage.error('获取用户数据失败：' + error.message);
    users.value = [];
    totalUsers.value = 0;
  } finally {
    loading.value = false;
    // 延迟设置isLoadingData为false，确保组件渲染完成
    setTimeout(() => {
      isLoadingData.value = false;
    }, 100);
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
  fetchUsers();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  currentPage.value = 1;
  fetchUsers();
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchUsers();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchUsers();
};

// 状态变更处理
const handleStatusChange = async (user, value) => {
  // 防止数据加载时触发
  if (isLoadingData.value) {
    return;
  }
  
  // 设置当前操作用户的加载状态
  user.isLoading = true;
  
  try {
    if (value) {
      // value为true，表示要解封用户（从禁用状态变为正常）
      // 当前状态应该是被禁用（status为false），要变为正常（status为true）
      await unbanUser(user.id);
      user.status = true;
      ElMessage.success(`用户 ${user.username} 已解封`);
    } else {
      // value为false，表示要禁用用户（从正常状态变为禁用）
      // 当前状态应该是正常（status为true），要变为禁用（status为false）
      await banUser(user.id);
      user.status = false;
      ElMessage.success(`用户 ${user.username} 已禁用`);
    }
  } catch (error) {
    ElMessage.error('操作失败：' + error.message);
    // 操作失败时不改变状态
  } finally {
    user.isLoading = false;
  }
};

// 查看用户详情
const viewUser = (user) => {
  selectedUser.value = { ...user };
  showUserDetail.value = true;
};



onMounted(() => {
  fetchUsers();
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
.user-management {
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

/* 用户详情 */
.user-detail {
  padding: 20px 0;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
}

.detail-item label {
  width: 100px;
  font-weight: 600;
  color: #666;
}

.detail-item span {
  flex: 1;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
  
  .table-container {
    overflow-x: auto;
  }
}
</style>
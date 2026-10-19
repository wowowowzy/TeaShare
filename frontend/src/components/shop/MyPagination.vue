<template>
  <div class="my-pagination">
    <!-- 显示常用的分页选项，同时保留输入框 -->
    <div class="pagination-controls">
      <div class="page-size-selector">
        <span>每页显示：</span>
        <el-input-number
          v-model="innerPageSize"
          :min="1"
          :max="total"
          :step="1"
          size="small"
          style="width: 80px; margin: 0 10px;"
          @change="handleInputSizeChange"
        />
        <span>条</span>
        <div class="quick-options">
          <button 
            v-for="option in quickOptions" 
            :key="option"
            class="quick-option-btn"
            :class="{ active: innerPageSize === option }"
            @click="setPageSize(option)"
          >
            {{ option }}
          </button>
        </div>
      </div>
      <div class="pagination-info">
        共 {{ total }} 条
      </div>
    </div>
    <el-pagination
      :current-page="currentPageProxy"
      :page-size="innerPageSize"
      layout="prev, pager, next, jumper"
      :total="total"
      @current-change="handleCurrentChangeProxy"
      @update:current-page="handleCurrentPageUpdate"
      prev-text="上一页"
      next-text="下一页"
      jumper-text="前往"
    />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

// 接收父组件的参数
const props = defineProps({
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  }
})

// 定义内部维护的 pageSize（和父组件同步）
const innerPageSize = ref(props.pageSize)

// 快速选择选项
const quickOptions = [5, 10, 15, 20, 30]

// 监听父组件的 pageSize 变化，同步到内部
watch(
  () => props.pageSize,
  (newVal) => {
    innerPageSize.value = newVal
  }
)

// 通过计算属性代理 currentPage，避免直接修改 prop
const currentPageProxy = computed({
  get() {
    return props.currentPage
  },
  set(val) {
    // 不直接修改 prop，而是通过事件通知父组件
    emit('update:current-page', val)
  }
})

// 每页条数改变时，通知父组件
const emit = defineEmits(['update:current-page', 'update:pageSize', 'size-change', 'current-change'])

const handleSizeChange = (val) => {
  // 更新内部 pageSize
  innerPageSize.value = val
  // 通知父组件
  emit('update:pageSize', val)
  emit('size-change', val)
}

const handleCurrentChange = (val) => {
  emit('current-change', val)
}

// 处理 el-pagination 的 current-page 更新
const handleCurrentPageUpdate = (val) => {
  // 通过代理修改，不直接修改 prop
  emit('update:current-page', val)
  emit('current-change', val)
}

// 处理分页组件的当前页变化
const handleCurrentChangeProxy = (val) => {
  // 通知父组件当前页变化
  emit('update:current-page', val)
  emit('current-change', val)
}

// 输入框变化时，触发 size-change
const handleInputSizeChange = (val) => {
  innerPageSize.value = val
  emit('update:pageSize', val)
  emit('size-change', val)
}

// 设置页面大小
const setPageSize = (val) => {
  innerPageSize.value = val
  emit('update:pageSize', val)
  emit('size-change', val)
}
</script>

<style scoped>
.my-pagination {
  width: 100%;
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  flex-wrap: wrap;
  gap: 15px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quick-options {
  display: flex;
  gap: 8px;
  margin-left: 10px;
}

.quick-option-btn {
  padding: 4px 10px;
  border: 1px solid #dcdfe6;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.quick-option-btn:hover {
  border-color: #2E7D32;
  color: #2E7D32;
}

.quick-option-btn.active {
  background-color: #2E7D32;
  color: white;
  border-color: #2E7D32;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

:deep(.el-pagination) {
  justify-content: center;
}

:deep(.el-pagination .el-pager li) {
  margin: 0 4px;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next) {
  padding: 0 12px;
}

@media (max-width: 768px) {
  .pagination-controls {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
  }
  
  .page-size-selector {
    flex-wrap: wrap;
  }
}
</style>
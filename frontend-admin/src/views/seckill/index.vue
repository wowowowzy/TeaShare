<template>
  <div class="flash-sale-management">
    <h2 class="page-title">秒杀管理</h2>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="openAddFlashSaleDialog">添加秒杀商品</el-button>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-input
        v-model="searchQuery"
        placeholder="搜索秒杀产品名称"
        prefix-icon="el-icon-search"
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>
    
    <!-- 秒杀活动表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      <el-table
        v-loading="loading"
        :data="filteredFlashSales"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column label="产品图片" width="100">
          <template #default="scope">
            <el-image
              :src="scope.row.goodsImage"
              fit="cover"
              style="width: 80px; height: 80px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="goodsId" label="商品ID" width="120" />
        <el-table-column prop="goodsName" label="产品名称" />
        <el-table-column prop="goodsPrice" label="秒杀价" width="100">
          <template #default="scope">
            ¥{{ scope.row.goodsPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stockNum" label="库存" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewFlashSale(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="editFlashSale(scope.row)">编辑</el-button>
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
        :total="totalFlashSales"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 秒杀商品详情弹窗 -->
    <el-dialog
      v-model="showFlashSaleDetail"
      title="秒杀商品详情"
      width="600px"
    >
      <div class="flash-sale-detail" v-if="selectedFlashSale">
        <div class="detail-row">
          <div class="detail-image">
            <el-image
              :src="selectedFlashSale.goodsImage"
              fit="cover"
              style="width: 200px; height: 200px"
            />
          </div>
          <div class="detail-info">
            <div class="detail-item">
              <label>商品ID：</label>
              <span>{{ selectedFlashSale.goodsId }}</span>
            </div>
            <div class="detail-item">
              <label>商品名称：</label>
              <span>{{ selectedFlashSale.goodsName }}</span>
            </div>
            <div class="detail-item">
              <label>秒杀价格：</label>
              <span class="flash-price">¥{{ selectedFlashSale.goodsPrice.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <label>库存：</label>
              <span>{{ selectedFlashSale.stockNum }}</span>
            </div>
            <div class="detail-item">
              <label>商品描述：</label>
              <span>{{ selectedFlashSale.goodsIntro }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 添加/编辑秒杀商品弹窗 -->
    <el-dialog
      v-model="showFlashSaleForm"
      :title="isEditing ? '编辑秒杀商品' : '添加秒杀商品'"
      width="600px"
    >
      <el-form :model="flashSaleForm" :rules="flashSaleRules" ref="flashSaleFormRef" label-width="100px">
        <el-form-item label="商品ID" prop="goodsId">
          <el-input v-model="flashSaleForm.goodsId" placeholder="请输入商品ID" type="number" />
        </el-form-item>
        <el-form-item label="秒杀价格" prop="goodsPrice">
          <el-input-number v-model="flashSaleForm.goodsPrice" :min="0" :step="0.01" placeholder="请输入秒杀价格" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="flashSaleForm.stock" :min="1" placeholder="请输入库存" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showFlashSaleForm = false">取消</el-button>
          <el-button type="primary" @click="submitFlashSaleForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { getSeckillGoods, setSeckillGoods } from '../../api/aseckil';

// 秒杀商品数据
const flashSales = ref([]);
const loading = ref(true);

// 搜索和筛选
const searchQuery = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const totalFlashSales = ref(0);

// 秒杀商品详情和表单
const showFlashSaleDetail = ref(false);
const selectedFlashSale = ref(null);
const showFlashSaleForm = ref(false);
const isEditing = ref(false);
const flashSaleForm = ref({
  goodsId: 0,
  stock: 0,
  goodsPrice: 0
});
const flashSaleFormRef = ref(null);

// 表单验证规则
const flashSaleRules = {
  goodsId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const num = Number(value);
        if (isNaN(num) || num <= 0 || !Number.isInteger(num)) {
          callback(new Error('商品ID必须为正整数'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 1, message: '库存至少为1', trigger: 'blur' }
  ],
  goodsPrice: [
    { required: true, message: '请输入秒杀价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ]
};

// 过滤秒杀商品
const filteredFlashSales = computed(() => {
  let result = flashSales.value;
  
  // 搜索过滤
  if (searchQuery.value) {
    result = result.filter(flashSale => 
      flashSale.goodsName.includes(searchQuery.value)
    );
  }
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return result.slice(start, end);
});

// 获取秒杀商品数据
const fetchSeckillGoods = async () => {
  loading.value = true;
  try {
    const response = await getSeckillGoods();
    flashSales.value = response || [];
    totalFlashSales.value = flashSales.value.length;
  } catch (error) {
    ElMessage.error('获取秒杀商品数据失败：' + error.message);
    flashSales.value = [];
    totalFlashSales.value = 0;
  } finally {
    loading.value = false;
  }
};



// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  totalFlashSales.value = flashSales.value.filter(flashSale => 
    flashSale.goodsName.includes(searchQuery.value)
  ).length;
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  currentPage.value = 1;
  totalFlashSales.value = flashSales.value.length;
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
};

// 查看秒杀商品详情
const viewFlashSale = (flashSale) => {
  selectedFlashSale.value = { ...flashSale };
  showFlashSaleDetail.value = true;
};

// 打开添加秒杀商品弹窗
const openAddFlashSaleDialog = () => {
  isEditing.value = false;
  flashSaleForm.value = {
    goodsId: 0,
    stock: 0,
    goodsPrice: 0
  };
  showFlashSaleForm.value = true;
};

// 编辑秒杀商品
const editFlashSale = (flashSale) => {
  isEditing.value = true;
  flashSaleForm.value = {
    goodsId: flashSale.goodsId,
    stock: flashSale.stockNum,
    goodsPrice: flashSale.goodsPrice
  };
  showFlashSaleForm.value = true;
};

// 提交秒杀商品表单
const submitFlashSaleForm = async () => {
  flashSaleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await setSeckillGoods(
          Number(flashSaleForm.value.goodsId),
          flashSaleForm.value.stock,
          flashSaleForm.value.goodsPrice
        );
        ElMessage.success(isEditing.value ? '秒杀商品编辑成功' : '秒杀商品添加成功');
        showFlashSaleForm.value = false;
        fetchSeckillGoods();
      } catch (error) {
        ElMessage.error('操作失败：' + error.message);
      }
    } else {
      ElMessage.error('请检查表单填写是否正确');
      return false;
    }
  });
};

onMounted(() => {
  fetchSeckillGoods();
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
.flash-sale-management {
  padding: 20px 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 30px;
  color: #333;
}

/* 操作按钮 */
.action-buttons {
  margin-bottom: 20px;
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

/* 秒杀商品详情 */
.flash-sale-detail {
  padding: 20px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 20px;
}

.detail-image {
  margin-right: 30px;
}

.detail-info {
  flex: 1;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
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

.flash-price {
  font-size: 18px;
  font-weight: 600;
  color: #e74c3c;
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
  
  .detail-row {
    flex-direction: column;
  }
  
  .detail-image {
    margin-right: 0;
    margin-bottom: 20px;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-item label {
    width: 100%;
    margin-bottom: 5px;
  }
}
</style>
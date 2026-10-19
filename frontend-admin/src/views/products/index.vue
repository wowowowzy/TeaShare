<template>
  <div class="product-management">
    <h2 class="page-title">产品管理</h2>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="openAddProductDialog">添加产品</el-button>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-input
        v-model="searchQuery"
        placeholder="搜索产品名称"
        prefix-icon="el-icon-search"
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="categoryFilter" placeholder="选择分类" class="filter-select">
        <el-option label="全部" value="" />
        <el-option label="绿茶" value="1" />
        <el-option label="红茶" value="2" />
        <el-option label="乌龙茶" value="3" />
        <el-option label="白茶" value="4" />
        <el-option label="黑茶" value="5" />
        <el-option label="特级茶" value="6" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>
    
    <!-- 产品表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      <el-table
        v-loading="loading"
        :data="products"
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
        <el-table-column prop="goodsId" label="产品ID" width="80" />
        <el-table-column prop="goodsName" label="产品名称" />
        <el-table-column prop="categoryId" label="分类" width="120">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="goodsPrice" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.goodsPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stockNum" label="库存" width="80" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewProduct(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="editProduct(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteProduct(scope.row.goodsId)">删除</el-button>
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
        :total="totalProducts"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 产品详情弹窗 -->
    <el-dialog
      v-model="showProductDetail"
      title="产品详情"
      width="600px"
    >
      <div class="product-detail" v-if="selectedProduct">
        <div class="detail-row">
          <div class="detail-image">
            <el-image
              :src="selectedProduct.goodsImage"
              fit="cover"
              style="width: 200px; height: 200px"
            />
          </div>
          <div class="detail-info">
            <div class="detail-item">
              <label>产品ID：</label>
              <span>{{ selectedProduct.goodsId }}</span>
            </div>
            <div class="detail-item">
              <label>产品名称：</label>
              <span>{{ selectedProduct.goodsName }}</span>
            </div>
            <div class="detail-item">
              <label>分类：</label>
              <span>{{ getCategoryName(selectedProduct.categoryId) }}</span>
            </div>
            <div class="detail-item">
              <label>价格：</label>
              <span>¥{{ selectedProduct.goodsPrice.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <label>库存：</label>
              <span>{{ selectedProduct.stockNum }}</span>
            </div>
            <div class="detail-item">
              <label>创建时间：</label>
              <span>{{ formatDate(selectedProduct.createTime) }}</span>
            </div>
            <div class="detail-item">
              <label>更新时间：</label>
              <span>{{ formatDate(selectedProduct.updateTime) }}</span>
            </div>
          </div>
        </div>
        <div class="detail-description">
          <label>产品描述：</label>
          <p>{{ selectedProduct.goodsIntro }}</p>
        </div>
      </div>
    </el-dialog>
    
    <!-- 添加/编辑产品弹窗 -->
    <el-dialog
      v-model="showProductForm"
      :title="isEditing ? '编辑产品' : '添加产品'"
      width="600px"
    >
      <el-form :model="productForm" :rules="productRules" ref="productFormRef" label-width="100px">
        <el-form-item label="产品名称" prop="goodsName">
          <el-input v-model="productForm.goodsName" placeholder="请输入产品名称" :disabled="isUploading" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" :disabled="isUploading">
            <el-option label="绿茶" value="1" />
            <el-option label="红茶" value="2" />
            <el-option label="乌龙茶" value="3" />
            <el-option label="白茶" value="4" />
            <el-option label="黑茶" value="5" />
            <el-option label="特级茶" value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="goodsPrice">
          <el-input-number v-model="productForm.goodsPrice" :min="0" :step="0.01" placeholder="请输入价格" :disabled="isUploading" />
        </el-form-item>
        <el-form-item label="库存" prop="stockNum">
          <el-input-number v-model="productForm.stockNum" :min="0" placeholder="请输入库存" :disabled="isUploading" />
        </el-form-item>
        <el-form-item label="产品图片" prop="goodsImage">
          <el-upload
            class="avatar-uploader"
            :action="''"
            :show-file-list="false"
            :on-change="handleImageUpload"
            :before-upload="beforeUpload"
            :disabled="isUploading"
          >
            <div v-if="isUploading" class="uploading-overlay">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>上传中...</span>
            </div>
            <img v-else-if="productForm._previewImage" :src="productForm._previewImage" class="avatar" />
            <img v-else-if="productForm.goodsImage" :src="productForm.goodsImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <el-input v-model="productForm.goodsImage" placeholder="也可以直接输入图片URL" style="margin-top: 10px" :disabled="isUploading" />
        </el-form-item>
        <el-form-item label="产品描述" prop="goodsIntro">
          <el-input
            v-model="productForm.goodsIntro"
            type="textarea"
            rows="4"
            placeholder="请输入产品描述"
            :disabled="isUploading"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showProductForm = false" :disabled="isUploading">取消</el-button>
          <el-button type="primary" @click="submitProductForm" :disabled="isUploading" :loading="isUploading">
            {{ isUploading ? '上传中...' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Loading } from '@element-plus/icons-vue';
import { getAllGoods, addGoods, updateGoods, deleteGoods, getGoodsDetail, uploadGoodsImage } from '../../api/agoods';

// 产品数据
const products = ref([]);
const loading = ref(true);

// 搜索和筛选
const searchQuery = ref('');
const categoryFilter = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const totalProducts = ref(0);

// 产品详情和表单
const showProductDetail = ref(false);
const selectedProduct = ref(null);
const showProductForm = ref(false);
const isEditing = ref(false);
const isUploading = ref(false); // 上传状态标志
const productForm = ref({
  goodsId: '',
  goodsName: '',
  categoryId: '',
  goodsPrice: 0,
  stockNum: 0,
  goodsImage: '',
  goodsIntro: ''
});
const productFormRef = ref(null);

// 表单验证规则
const productRules = {
  goodsName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '产品名称长度在2-50之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择产品分类', trigger: 'change' }
  ],
  goodsPrice: [
    { required: true, message: '请输入产品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  stockNum: [
    { required: true, message: '请输入产品库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能为负数', trigger: 'blur' }
  ],
  goodsImage: [
    { required: true, message: '请上传产品图片', trigger: 'change' }
  ],
  goodsIntro: [
    { required: true, message: '请输入产品描述', trigger: 'blur' },
    { min: 2, message: '产品描述至少2个字符', trigger: 'blur' }
  ]
};

// 分类映射
const categoryMap = {
  '1': '绿茶',
  '2': '红茶',
  '3': '乌龙茶',
  '4': '白茶',
  '5': '黑茶',
  '6': '特级茶'
};

// 反向映射：分类名称到ID
const categoryNameToId = {
  '绿茶': '1',
  '红茶': '2',
  '乌龙茶': '3',
  '白茶': '4',
  '黑茶': '5',
  '特级茶': '6'
};

// 获取分类名称
const getCategoryName = (categoryId) => {
  return categoryMap[categoryId] || '未知分类';
};

// 获取产品数据
const fetchProducts = async () => {
  loading.value = true;
  try {
    const response = await getAllGoods(currentPage.value, pageSize.value, searchQuery.value, categoryFilter.value);
    products.value = response.records || [];
    totalProducts.value = response.total || 0;
  } catch (error) {
    ElMessage.error('获取产品数据失败：' + error.message);
    products.value = [];
    totalProducts.value = 0;
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
  fetchProducts();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  categoryFilter.value = '';
  currentPage.value = 1;
  fetchProducts();
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchProducts();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchProducts();
};

// 查看产品详情
const viewProduct = async (product) => {
  try {
    const detail = await getGoodsDetail(product.goodsId);
    selectedProduct.value = detail;
    showProductDetail.value = true;
  } catch (error) {
    ElMessage.error('获取产品详情失败：' + error.message);
  }
};

// 打开添加产品弹窗
const openAddProductDialog = () => {
  isEditing.value = false;
  productForm.value = {
    goodsId: '',
    goodsName: '',
    categoryId: '',
    goodsPrice: 0,
    stockNum: 0,
    goodsImage: '',
    goodsIntro: '',
    _previewImage: ''
  };
  showProductForm.value = true;
};

// 编辑产品
const editProduct = (product) => {
  isEditing.value = true;
  productForm.value = { 
    ...product,
    _previewImage: '' // 编辑时不显示本地预览，使用已有URL
  };
  showProductForm.value = true;
};

// 删除产品
const deleteProduct = (goodsId) => {
  ElMessageBox.confirm('确定要删除这个产品吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteGoods(goodsId);
      ElMessage.success('产品删除成功');
      fetchProducts();
    } catch (error) {
      ElMessage.error('删除产品失败：' + error.message);
    }
  }).catch(() => {
    // 取消删除
  });
};

// 图片上传处理
const handleImageUpload = async (file) => {
  // 防止重复上传
  if (isUploading.value) {
    ElMessage.info('图片正在上传中，请稍后再试');
    return false;
  }
  
  // 先使用base64进行本地预览
  const reader = new FileReader();
  reader.onload = async (e) => {
    // 显示本地预览
    const previewUrl = e.target.result;
    productForm.value._previewImage = previewUrl;
    
    try {
      // 设置上传状态
      isUploading.value = true;
      
      // 调用上传接口获取图片URL
      ElMessage.info('正在上传图片...');
      const response = await uploadGoodsImage(file.raw);
      
      // 假设后端返回的URL在response.data或response中
      const imageUrl = response.data || response;
      productForm.value.goodsImage = imageUrl;
      ElMessage.success('图片上传成功');
    } catch (error) {
      ElMessage.error('图片上传失败：' + error.message);
      // 上传失败时清除预览
      productForm.value._previewImage = '';
    } finally {
      // 无论成功失败，都重置上传状态
      isUploading.value = false;
    }
  };
  reader.readAsDataURL(file.raw);
  return false; // 阻止自动上传
};

const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isJPG) {
    ElMessage.error('只能上传JPG/PNG图片');
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB');
  }
  return isJPG && isLt2M;
};

// 提交产品表单
const submitProductForm = async () => {
  productFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备提交数据，移除不需要的字段
        const submitData = {
          goodsName: productForm.value.goodsName,
          goodsIntro: productForm.value.goodsIntro,
          goodsPrice: productForm.value.goodsPrice,
          categoryId: Number(productForm.value.categoryId), // 确保是数字类型
          stockNum: productForm.value.stockNum,
          goodsImage: productForm.value.goodsImage
        };
        
        if (isEditing.value) {
          // 编辑产品时添加goodsId
          submitData.goodsId = productForm.value.goodsId;
          await updateGoods(submitData);
          ElMessage.success('产品编辑成功');
        } else {
          // 添加产品 - 不传递goodsId
          await addGoods(submitData);
          ElMessage.success('产品添加成功');
        }
        showProductForm.value = false;
        fetchProducts();
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
  fetchProducts();
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
.product-management {
  padding: 20px 0;
  overflow: hidden;
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

/* 产品详情 */
.product-detail {
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
}

.detail-item label {
  width: 80px;
  font-weight: 600;
  color: #666;
}

.detail-item span {
  flex: 1;
  color: #333;
}

.detail-description {
  margin-top: 20px;
}

.detail-description label {
  display: block;
  font-weight: 600;
  color: #666;
  margin-bottom: 10px;
}

.detail-description p {
  color: #333;
  line-height: 1.6;
}

/* 图片上传 */
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}

/* 上传中覆盖层 */
.uploading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 6px;
}

.uploading-overlay .el-icon {
  font-size: 32px;
  color: #409EFF;
  margin-bottom: 10px;
}

.uploading-overlay span {
  color: #666;
  font-size: 14px;
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
  
  .detail-row {
    flex-direction: column;
  }
  
  .detail-image {
    margin-right: 0;
    margin-bottom: 20px;
  }
}
</style>
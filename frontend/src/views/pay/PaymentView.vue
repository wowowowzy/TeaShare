<template>
  <div class="payment">
    <!-- 导航栏组件 -->
    <Navbar />

    <!-- 支付内容 -->
    <div class="payment-content">
      <h1 class="section-title">支付</h1>
      
      <!-- 主要内容区域 -->
      <div class="payment-main">
        <!-- 左侧内容 -->
        <div class="payment-left">
          <!-- 地址管理 -->
          <div class="address-section">
            <div class="section-header">
              <h2>收货地址</h2>
              <button class="btn-add" @click="openAddAddressDialog">
                <i class="fas fa-plus"></i> 添加地址
              </button>
            </div>
            <div v-if="addresses.length > 0">
              <el-scrollbar height="320px">
                <el-card
                  v-for="address in addresses"
                  :key="address.id"
                  class="address-card"
                  :class="{ active: selectedAddressId === address.id }"
                  :body-style="{ padding: '16px' }"
                  @click="selectAddress(address.id)"
                >
                  <div class="address-header">
                    <div class="address-info">
                      <span class="address-name">{{ address.name }}</span>
                      <span class="address-phone">{{ address.phone }}</span>
                      <el-tag v-if="address.isDefault == 1" size="small" type="primary">默认</el-tag>
                    </div>
                    <!-- <div class="address-radio">
                      <el-radio v-model="selectedAddressId" :label="address.id" border></el-radio>
                    </div> -->
                  </div>
                  <div class="address-content">
                    <el-descriptions :column="1" border>
                      <el-descriptions-item label="地址">
                        {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
                      </el-descriptions-item>
                      <el-descriptions-item label="邮编" v-if="address.postalCode">
                        {{ address.postalCode }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                  <div class="address-actions">
                    <el-button type="primary" size="small" @click.stop="editAddress(address)">
                      <el-icon><Edit /></el-icon> 编辑
                    </el-button>
                    <el-button type="danger" size="small" :disabled="address.isDefault == 1" @click.stop="deleteAddressHandler(address.id)">
                      <el-icon><Delete /></el-icon> 删除
                    </el-button>
                  </div>
                </el-card>
              </el-scrollbar>
            </div>
            <div v-else class="empty-address">
              <el-empty description="暂无收货地址">
                <el-button type="primary" @click="openAddAddressDialog">添加收货地址</el-button>
              </el-empty>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="order-info">
            <h2>商品信息</h2>
            <div class="order-items-container">
              <div class="order-item" v-for="item in orderItems" :key="item.goodsId">
                <div class="item-image">
                  <img :src="item.goodsImage || item.image || 'https://picsum.photos/80/80'" :alt="item.goodsName || item.name" />
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.goodsName || item.name }}</span>
                  <span class="item-specs">{{ item.specs }}</span>
                </div>
                <div class="item-price">
                  <span class="price">¥{{ (parseFloat(item.goodsPrice || item.price || 0)).toFixed(2) }}</span>
                  <span class="quantity">x{{ item.quantity || 0 }}</span>
                  <span class="subtotal">小计：¥{{ ((parseFloat(item.goodsPrice || item.price || 0) * (parseInt(item.quantity || 0)))).toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧内容 -->
        <div class="payment-right">
          <!-- 优惠券 -->
          <div class="coupon-section">
            <div class="section-header">
              <h2>优惠券</h2>
              <button
                class="btn-link"
                @click="toggleCouponList"
                :disabled="!canUseCoupon.canUse"
                :class="{ disabled: !canUseCoupon.canUse }"
              >
                {{ showCouponList ? '收起' : '查看可用优惠券' }}
              </button>
            </div>
            <div class="coupon-disabled-hint" v-if="!canUseCoupon.canUse">
              <el-alert
                :title="canUseCoupon.reason"
                type="warning"
                :closable="false"
                show-icon
              />
            </div>
            <div class="coupon-list" v-show="showCouponList && canUseCoupon.canUse">
              <div
                class="coupon-item"
                v-for="coupon in availableCoupons"
                :key="coupon.id"
                :class="{ active: selectedCouponId === coupon.id }"
                @click="selectCoupon(coupon.id)"
              >
                <div class="coupon-value">
                  <span class="coupon-amount">¥{{ coupon.reduceAmount }}</span>
                </div>
                <div class="coupon-info">
                  <span class="coupon-name">{{ coupon.intro }}</span>
                  <span class="coupon-date">有效期至: {{ coupon.endTime }}</span>
                </div>
              </div>
              <div v-if="availableCoupons.length === 0" class="empty-coupon">暂无可用优惠券</div>
            </div>
            <div class="selected-coupon" v-if="selectedCouponId && canUseCoupon.canUse">
              <span>已选择优惠券：</span>
              <span class="coupon-name">{{ getSelectedCouponName() }}</span>
              <button class="btn-remove" @click="removeCoupon">移除</button>
            </div>
          </div>

          <!-- AI小茗助手 - 绝对定位悬浮在优惠券右侧 -->
          <div class="ai-assistant" @click="goToAI">
            <div class="ai-button">
              <img src="/assest/number1.png" alt="AI Assistant" />
            </div>
            <p class="ai-text">点击图片可以与小茗对话哦</p>
          </div>

          <!-- 价格汇总 -->
          <div class="price-summary">
            <div class="summary-item">
              <span>商品总价</span>
              <span>¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>运费</span>
              <span>¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="summary-item" v-if="selectedCouponId">
              <span>优惠券</span>
              <span class="discount">-¥{{ getCouponDiscount().toFixed(2) }}</span>
            </div>
            <div class="summary-item total">
              <span>实付金额</span>
              <span>¥{{ finalPrice.toFixed(2) }}</span>
            </div>
          </div>

          <!-- 支付按钮 -->
          <div class="payment-actions">
            <button 
              class="btn btn-primary" 
              @click="processPayment"
              :disabled="!selectedAddressId || orderItems.length === 0"
            >
              确认支付 ¥{{ finalPrice.toFixed(2) }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑地址对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑收货地址' : '添加收货地址'"
      width="500px"
    >
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef">
        <el-form-item label="收件人姓名" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收件人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份"></el-input>
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市"></el-input>
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县"></el-input>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" placeholder="请输入详细地址"></el-input>
        </el-form-item>
        <el-form-item label="邮政编码" prop="postalCode">
          <el-input v-model="addressForm.postalCode" placeholder="请输入邮政编码"></el-input>
        </el-form-item>
        <el-form-item label="是否默认地址" prop="isDefault">
          <el-radio-group v-model="addressForm.isDefault">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddressForm">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import Navbar from '../../components/Navbar.vue';
import { cartApi, payApi, paySeckillGoods } from '../../api/cart';
import { getAddressList, addAddress, updateAddress, deleteAddress, getCoupons } from '../../api/user';
import { ElMessage, ElMessageBox, ElScrollbar, ElCard, ElTag, ElDescriptions, ElEmpty, ElButton, ElIcon, ElRadio } from 'element-plus';
import { Edit, Delete } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

const goToAI = () => {
  router.push('/quiz');
};

// 订单数据
const orderItems = ref([]);
const shippingFee = ref(0);
const loading = ref(false);

// 地址管理
const addresses = ref([]);
const selectedAddressId = ref(null);
const dialogVisible = ref(false);
const isEditing = ref(false);
const currentAddressId = ref(null);
const addressForm = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  postalCode: '',
  isDefault: 0
});
const addressFormRef = ref(null);

// 地址表单验证规则
const addressRules = {
  name: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码格式', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区县', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '详细地址长度应在5-200个字符之间', trigger: 'blur' }
  ],
  postalCode: [
    { pattern: /^\d{6}$/, message: '请输入正确的6位邮政编码', trigger: 'blur' }
  ],
  isDefault: [
    { required: true, message: '请选择是否默认地址', trigger: 'change' }
  ]
};

// 优惠券管理
const availableCoupons = ref([]);
const selectedCouponId = ref(null);
const showCouponList = ref(true);

// 计算总价
const totalPrice = computed(() => {
  return orderItems.value.reduce((total, item) => {
    if (item) {
      const price = parseFloat(item.goodsPrice || item.price || 0);
      const quantity = parseInt(item.quantity || 0);
      return total + price * quantity;
    }
    return total;
  }, 0);
});

// 计算优惠券折扣
const getCouponDiscount = () => {
  const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value);
  return coupon ? coupon.reduceAmount : 0;
};

// 计算最终价格
const finalPrice = computed(() => {
  let price = totalPrice.value + shippingFee.value;
  price -= getCouponDiscount();
  return Math.max(0, price);
});

// 获取选中的优惠券名称
const getSelectedCouponName = () => {
  const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value);
  return coupon ? coupon.intro : '';
};

// 页面加载时获取数据
onMounted(() => {
  loadCartItems();
  loadAddresses();
  loadCoupons();
});

// 加载购物车商品或秒杀商品
async function loadCartItems() {
  loading.value = true;
  try {
    // 检查是否是秒杀商品
    const isSeckill = route.query.seckill === 'true';
    // 检查是否是立即购买
    const isBuyNow = route.query.buyNow === 'true';
    console.log('是否是秒杀商品:', isSeckill);
    console.log('是否是立即购买:', isBuyNow);
    console.log('URL参数:', route.query);

    if (isSeckill) {
      // 处理秒杀商品
      const goodsId = route.query.goodsId;
      const goodsName = route.query.goodsName;
      const goodsPrice = route.query.goodsPrice;
      const goodsImage = route.query.goodsImage;
      const quantity = route.query.quantity || '1';

      console.log('秒杀商品信息:', { goodsId, goodsName, goodsPrice, goodsImage, quantity });

      if (goodsId && goodsName && goodsPrice) {
        const price = parseFloat(goodsPrice);
        console.log('解析后的价格:', price);

        if (isNaN(price) || price <= 0) {
          console.error('价格参数无效:', goodsPrice);
          ElMessage.error('商品价格信息无效');
          router.push('/shop');
          return;
        }

        orderItems.value = [{
          goodsId: goodsId,
          goodsName: decodeURIComponent(goodsName),
          goodsPrice: price,
          quantity: parseInt(quantity),
          specs: "秒杀商品",
          goodsImage: goodsImage ? decodeURIComponent(goodsImage) : 'https://picsum.photos/800/800?random=999'
        }];

        console.log('订单商品信息:', orderItems.value);
      } else {
        console.error('秒杀商品信息不完整:', { goodsId, goodsName, goodsPrice });
        ElMessage.error('秒杀商品信息不完整');
        router.push('/shop');
      }
    } else if (isBuyNow) {
      // 处理立即购买商品
      const goodsId = route.query.goodsId;
      const goodsName = route.query.goodsName;
      const goodsPrice = route.query.goodsPrice;
      const goodsImage = route.query.goodsImage;
      const quantity = route.query.quantity || '1';

      console.log('立即购买商品信息:', { goodsId, goodsName, goodsPrice, goodsImage, quantity });

      if (goodsId && goodsName && goodsPrice) {
        const price = parseFloat(goodsPrice);
        console.log('解析后的价格:', price);

        if (isNaN(price) || price <= 0) {
          console.error('价格参数无效:', goodsPrice);
          ElMessage.error('商品价格信息无效');
          router.push('/shop');
          return;
        }

        orderItems.value = [{
          goodsId: goodsId,
          goodsName: decodeURIComponent(goodsName),
          goodsPrice: price,
          quantity: parseInt(quantity),
          specs: "",
          goodsImage: goodsImage ? decodeURIComponent(goodsImage) : 'https://picsum.photos/800/800?random=999'
        }];

        console.log('订单商品信息:', orderItems.value);
      } else {
        console.error('商品信息不完整:', { goodsId, goodsName, goodsPrice });
        ElMessage.error('商品信息不完整');
        router.push('/shop');
      }
    } else {
      // 处理普通购物车商品
      const cartList = await cartApi.getCartList();
      if (cartList && cartList.length > 0) {
        orderItems.value = cartList;
      } else {
        // 使用模拟数据
        orderItems.value = [
          {
            goodsId: 1,
            name: "西湖龙井 明前特级",
            price: 298,
            quantity: 1,
            specs: "规格：250g/罐 | 产地：浙江杭州",
            image: "https://picsum.photos/800/800?random=20"
          },
          {
            goodsId: 2,
            name: "云南普洱熟茶 七子饼茶",
            price: 168,
            quantity: 2,
            specs: "规格：357g/饼 | 年份：2018年",
            image: "https://picsum.photos/800/800?random=21"
          }
        ];
      }
    }
  } catch (error) {
    console.error('获取商品失败:', error);
    ElMessage.error('获取商品失败');
    // 使用模拟数据
    orderItems.value = [
      {
        goodsId: 1,
        name: "西湖龙井 明前特级",
        price: 298,
        quantity: 1,
        specs: "规格：250g/罐 | 产地：浙江杭州",
        image: "https://picsum.photos/800/800?random=20"
      },
      {
        goodsId: 2,
        name: "云南普洱熟茶 七子饼茶",
        price: 168,
        quantity: 2,
        specs: "规格：357g/饼 | 年份：2018年",
        image: "https://picsum.photos/800/800?random=21"
      }
    ];
  } finally {
    loading.value = false;
  }
}

// 加载地址列表
async function loadAddresses() {
  try {
    const addressList = await getAddressList();
    // 检查数据格式，确保获取到正确的地址列表
    let validAddressList = [];
    if (Array.isArray(addressList)) {
      validAddressList = addressList;
    } else if (addressList && addressList.data && Array.isArray(addressList.data)) {
      validAddressList = addressList.data;
    } else {
      console.warn('地址数据格式错误:', addressList);
    }
    
    addresses.value = validAddressList;
    // 选择默认地址
    const defaultAddress = validAddressList.find(addr => addr.isDefault === 1);
    if (defaultAddress) {
      selectedAddressId.value = defaultAddress.id;
    } else if (validAddressList.length > 0) {
      selectedAddressId.value = validAddressList[0].id;
    }
  } catch (error) {
    console.error('获取地址列表失败:', error);
    ElMessage.error('获取地址列表失败');
    addresses.value = [];
  }
}

// 加载优惠券列表
async function loadCoupons() {
  try {
    const response = await getCoupons();
    // 检查响应结构，确保数据格式正确
    if (response && Array.isArray(response)) {
      availableCoupons.value = response;
    } else if (response && response.data && Array.isArray(response.data)) {
      availableCoupons.value = response.data;
    } else {
      console.error('优惠券数据格式错误:', response);
      // 使用模拟数据
      availableCoupons.value = [
        {
          id: 1,
          intro: "满200减30",
          reduceAmount: 30,
          endTime: "2026-12-31"
        },
        {
          id: 2,
          intro: "满100减15",
          reduceAmount: 15,
          endTime: "2026-12-31"
        }
      ];
    }
  } catch (error) {
    console.error('获取优惠券失败:', error);
    ElMessage.error('获取优惠券失败');
    // 使用模拟数据
    availableCoupons.value = [
      {
        id: 1,
        intro: "满200减30",
        reduceAmount: 30,
        endTime: "2026-12-31"
      },
      {
        id: 2,
        intro: "满100减15",
        reduceAmount: 15,
        endTime: "2026-12-31"
      }
    ];
  }
}

// 选择地址
function selectAddress(addressId) {
  selectedAddressId.value = addressId;
}

// 打开添加地址对话框
function openAddAddressDialog() {
  // 重置表单为空白状态
  addressForm.value = {
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    postalCode: '',
    isDefault: 0
  };
  // 重置编辑状态
  isEditing.value = false;
  currentAddressId.value = null;
  dialogVisible.value = true;
}

// 编辑地址
function editAddress(address) {
  // 设置编辑模式
  isEditing.value = true;
  currentAddressId.value = address.id;
  
  // 填充表单数据
  addressForm.value = {
    name: address.name || '',
    phone: address.phone || '',
    province: address.province || '',
    city: address.city || '',
    district: address.district || '',
    detail: address.detail || '',
    postalCode: address.postalCode || '',
    isDefault: address.isDefault || 0
  };
  
  // 打开对话框
  dialogVisible.value = true;
}

// 提交地址表单
async function submitAddressForm() {
  if (!addressFormRef.value) {
    ElMessage.error('表单初始化失败');
    return;
  }
  
  try {
    await addressFormRef.value.validate();
    
    // 提交表单数据
    let response;
    if (isEditing.value) {
      // 编辑模式
      response = await updateAddress({ ...addressForm.value, id: currentAddressId.value });
    } else {
      // 添加模式
      response = await addAddress(addressForm.value);
    }
    
    if (response.code === 1) {
      ElMessage.success(isEditing.value ? '地址修改成功' : '地址添加成功');
      dialogVisible.value = false;
      loadAddresses();
    } else {
      ElMessage.error(isEditing.value ? '地址修改失败' : '地址添加失败');
    }
  } catch (error) {
    if (error.name === 'Error') {
      ElMessage.error(error.message);
    } else {
      ElMessage.error('表单验证失败');
    }
  }
}

// 删除地址
async function deleteAddressHandler(id) {
  // 确认删除
  ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteAddress(id);
      if (response.code === 1) {
        ElMessage.success('地址删除成功');
        loadAddresses();
      } else {
        ElMessage.error('地址删除失败');
      }
    } catch (error) {
      console.error('删除地址失败:', error);
      ElMessage.error('删除地址失败');
    }
  }).catch(() => {
    // 取消删除
  });
}

// 检查是否是秒杀商品
const isSeckillGoods = computed(() => {
  return route.query.seckill === 'true';
});

// 检查是否可以使用优惠券
const canUseCoupon = computed(() => {
  // 秒杀商品不能使用优惠券
  if (isSeckillGoods.value) {
    return { canUse: false, reason: '秒杀商品不支持使用优惠券' };
  }
  // 商品金额不足200元不能使用优惠券
  if (totalPrice.value < 200) {
    return { canUse: false, reason: '商品金额不足200元，无法使用优惠券' };
  }
  return { canUse: true, reason: '' };
});

// 切换优惠券列表
function toggleCouponList() {
  // 检查是否可以使用优惠券
  const check = canUseCoupon.value;
  if (!check.canUse) {
    ElMessage.warning(check.reason);
    return;
  }
  showCouponList.value = !showCouponList.value;
}

// 选择优惠券
function selectCoupon(couponId) {
  // 检查是否可以使用优惠券
  const check = canUseCoupon.value;
  if (!check.canUse) {
    ElMessage.warning(check.reason);
    return;
  }
  selectedCouponId.value = couponId;
  showCouponList.value = false;
}

// 移除优惠券
function removeCoupon() {
  selectedCouponId.value = null;
}

// 处理支付
async function processPayment() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址');
    return;
  }

  if (orderItems.value.length === 0) {
    ElMessage.warning('商品为空');
    return;
  }

  // 支付前校验优惠券使用条件
  if (selectedCouponId.value) {
    const check = canUseCoupon.value;
    if (!check.canUse) {
      ElMessage.warning(check.reason);
      // 自动移除不符合条件的优惠券
      selectedCouponId.value = null;
      return;
    }
  }

  loading.value = true;
  try {
    // 准备支付数据
    const goodList = orderItems.value.map(item => ({
      goodsId: item.goodsId,
      quantity: item.quantity
    }));

    // 检查是否是秒杀商品或立即购买
    const isSeckill = route.query.seckill === 'true';
    const isBuyNow = route.query.buyNow === 'true';
    console.log('是否是秒杀商品:', isSeckill);
    console.log('是否是立即购买:', isBuyNow);
    console.log('支付数据:', { goodList, couponId: selectedCouponId.value });

    // 调用支付接口
    let response;
    if (isSeckill) {
      console.log('调用秒杀商品支付接口');
      // 秒杀商品只需要传递goodsId
      const goodsId = orderItems.value[0].goodsId;
      console.log('秒杀商品ID:', goodsId);
      response = await paySeckillGoods(goodsId);
      console.log('秒杀商品支付接口响应:', response);
    } else {
      console.log('调用普通商品支付接口');
      response = await payApi(selectedCouponId.value, goodList);
      console.log('普通商品支付接口响应:', response);
    }

    console.log('支付接口返回结果:', response);
    console.log('响应类型:', typeof response);
    console.log('响应结构:', JSON.stringify(response, null, 2));

    // 检查响应结构，可能后端返回的是不同的格式
    let isPaymentSuccess = false;
    let successMessage = '支付成功！';

    if (typeof response === 'object') {
      // 检查是否是成功响应
      if (response.code === 1 || response.success === true || response.status === 'success') {
        isPaymentSuccess = true;
        // 提取data字段作为提示信息
        if (response.data) {
          successMessage = response.data;
        }
      }
    } else if (typeof response === 'string') {
      if (response.includes('success') || response.includes('成功')) {
        isPaymentSuccess = true;
        successMessage = response;
      }
    }

    console.log('支付是否成功:', isPaymentSuccess);
    console.log('成功提示信息:', successMessage);

    if (isPaymentSuccess) {
      ElMessage.success(successMessage);

      // 清空购物车（仅非秒杀商品且非立即购买）
      if (!isSeckill && !isBuyNow) {
        try {
          const goodsIds = orderItems.value.map(item => item.goodsId);
          await cartApi.deleteCartItems(goodsIds);
          console.log('购物车已清空');
        } catch (error) {
          console.error('清空购物车失败:', error);
          // 即使清空购物车失败，也继续跳转到支付成功页面
        }
      }

      // 跳转到支付成功页面
      router.push({
        path: '/payment/success',
        query: {
          amount: finalPrice.value.toFixed(2),
          seckill: isSeckill ? 'true' : 'false'
        }
      });
    } else {
      // 提取错误信息
      let errorMessage = '支付失败，请重试';
      if (typeof response === 'object') {
        if (response.msg) {
          errorMessage = response.msg;
        } else if (response.message) {
          errorMessage = response.message;
        } else if (response.error) {
          errorMessage = response.error;
        }
      } else if (typeof response === 'string') {
        errorMessage = response;
      }
      console.error('支付失败，错误信息:', errorMessage);
      ElMessage.error(errorMessage);
      // 支付失败时，释放商品库存（后端处理）
    }
  } catch (error) {
    console.error('支付接口调用失败:', error);
    ElMessage.error('支付失败，请检查网络连接');
    // 支付失败时，释放商品库存（后端处理）
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.payment {
  min-height: 100vh;
  padding-top: 80px;
  background-color: #f5f5f5;
}

.payment-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.section-title {
  font-size: 32px;
  color: #000000;
  margin-bottom: 40px;
  text-align: center;
  font-weight: 600;
}

.payment-main {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 30px;
  position: relative;
}

/* 左侧内容 */
.payment-left {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 地址管理 */
.address-section {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  color: #000000;
  font-weight: 600;
  margin: 0;
}

.btn-add {
  background: #1976d2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-add:hover {
  background: #1565c0;
  transform: translateY(-2px);
}

.btn-add i {
  margin-right: 6px;
}

/* 地址管理 */
.address-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-card:hover {
  box-shadow: 0 4px 16px rgba(25, 118, 210, 0.15);
  transform: translateY(-2px);
}

.address-card.active {
  border-color: #1976d2;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  box-shadow: 0 4px 16px rgba(25, 118, 210, 0.2);
}

/* 地址卡片头部 */
.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e0e0e0;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.address-name {
  font-weight: 700;
  color: #1a1a1a;
  font-size: 17px;
}

.address-phone {
  color: #666;
  font-size: 15px;
  font-family: 'Consolas', monospace;
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: 20px;
}

.address-radio {
  display: flex;
  align-items: center;
}

/* 地址卡片内容 */
.address-content {
  margin-bottom: 16px;
}

/* 地址卡片操作按钮 */
.address-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 空地址状态 */
.empty-address {
  padding: 40px 20px;
  text-align: center;
}

/* 商品信息 */
.order-info {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.order-info h2 {
  font-size: 18px;
  color: #000000;
  font-weight: 600;
  margin-bottom: 20px;
}

.order-items-container {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.order-items-container::-webkit-scrollbar {
  width: 6px;
}

.order-items-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.order-items-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.order-items-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.item-image {
  width: 80px;
  height: 80px;
  margin-right: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.item-info {
  flex: 1;
  padding-right: 20px;
}

.item-name {
  display: block;
  font-size: 16px;
  font-weight: 500;
  color: #000000;
  margin-bottom: 8px;
}

.item-specs {
  display: block;
  font-size: 14px;
  color: #666666;
  line-height: 1.4;
}

.item-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  min-width: 120px;
}

.price {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
}

.quantity {
  font-size: 14px;
  color: #666666;
}

.subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
}

/* 右侧内容 */
.payment-right {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 优惠券 */
.coupon-section {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

/* AI小茗助手 - 悬浮在优惠券右侧 */
.ai-assistant {
  position: fixed;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 15px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  z-index: 1000;
  min-width: 80px;
}

.ai-assistant:hover {
  box-shadow: 0 6px 24px rgba(238, 90, 90, 0.3);
  transform: translateY(-50%) scale(1.05);
}

.ai-button {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #ffffff 0%, #ffffff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 5px;
}

.ai-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(238, 90, 90, 0.6);
}

.ai-button img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.ai-text {
  margin-top: 10px;
  color: #5e5b5b;
  font-size: 12px;
  font-weight: bold;
  background-color: #fff;
  padding: 4px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  text-align: center;
}

.btn-link {
  background: none;
  border: none;
  color: #1976d2;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  padding: 0;
  transition: all 0.3s ease;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link:disabled,
.btn-link.disabled {
  color: #999;
  cursor: not-allowed;
}

.btn-link:disabled:hover,
.btn-link.disabled:hover {
  text-decoration: none;
}

.coupon-disabled-hint {
  margin-top: 16px;
}

.coupon-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.coupon-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.coupon-item:hover {
  border-color: #1976d2;
  box-shadow: 0 2px 12px rgba(25, 118, 210, 0.1);
}

.coupon-item.active {
  border-color: #1976d2;
  background-color: #f0f7ff;
}

.coupon-value {
  display: flex;
  align-items: center;
  padding-right: 16px;
  border-right: 2px dashed #e0e0e0;
  min-width: 80px;
}

.coupon-amount {
  font-size: 20px;
  font-weight: 700;
  color: #000000;
}

.coupon-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.coupon-name {
  font-weight: 600;
  color: #000000;
  font-size: 14px;
}

.coupon-date {
  font-size: 12px;
  color: #666666;
}

.selected-coupon {
  margin-top: 16px;
  padding: 12px;
  background-color: #f0f7ff;
  border: 1px solid #1976d2;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.btn-remove {
  background: none;
  border: none;
  color: #f44336;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  padding: 0;
  transition: all 0.3s ease;
}

.btn-remove:hover {
  text-decoration: underline;
}

.empty-coupon {
  text-align: center;
  padding: 30px 15px;
  color: #666666;
  font-size: 14px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-top: 10px;
}

/* 价格汇总 */
.price-summary {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.price-summary h2 {
  font-size: 18px;
  color: #000000;
  font-weight: 600;
  margin-bottom: 20px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  color: #000000;
}

.summary-item.discount {
  color: #f44336;
}

.summary-item.total {
  font-weight: bold;
  font-size: 18px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 支付按钮 */
.payment-actions {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.btn-primary {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 8px;
  background-color: #1976d2;
  color: white;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
  font-weight: 600;
}

.btn-primary:hover {
  background-color: #1565c0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.3);
}

.btn-primary:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .ai-assistant {
    right: 20px;
  }
  
  .ai-button {
    width: 60px;
    height: 60px;
  }
  
  .ai-text {
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .payment-main {
    grid-template-columns: 1fr;
  }
  
  .payment-content {
    padding: 20px 16px;
  }
  
  .section-title {
    font-size: 24px;
    margin-bottom: 30px;
  }
  
  .order-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .item-image {
    width: 100%;
    height: 200px;
    margin-right: 0;
  }
  
  .item-price {
    align-items: flex-start;
    width: 100%;
  }
  
  .ai-assistant {
    right: 15px;
    padding: 10px;
  }
  
  .ai-button {
    width: 50px;
    height: 50px;
  }
  
  .ai-text {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .ai-assistant {
    right: 10px;
    padding: 8px;
  }
  
  .ai-button {
    width: 45px;
    height: 45px;
  }
  
  .ai-text {
    font-size: 9px;
    white-space: normal;
    text-align: center;
    max-width: 60px;
  }
}
</style>
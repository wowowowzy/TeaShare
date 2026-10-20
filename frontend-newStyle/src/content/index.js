import teaVarieties from './tea-varieties.json'
import teaMaking from './tea-making.json'
import teaHistory from './tea-history.json'
import teaware from './teaware.json'
import masters from './masters.json'
import shop from './shop.json'

export { teaVarieties, teaMaking, teaHistory, teaware, masters, shop }

/** 订单状态 → 文案 */
export const ORDER_STATUS_MAP = {
  0: '未审核',
  1: '已付款',
  2: '已发货',
  3: '已送达',
  4: '已取消',
  PENDING_PAYMENT: '待付款',
  PAID: '已付款',
  SHIPPED: '已发货',
  DELIVERED: '已送达',
  CANCELLED: '已取消'
}

/** 订单状态 → 描述 */
export const ORDER_DESC_MAP = {
  0: '订单正在审核中',
  1: '商家正在准备发货',
  2: '商品正在配送中',
  3: '感谢您的购买',
  4: '订单已取消',
  PENDING_PAYMENT: '请尽快完成支付'
}

export const PAY_METHOD_MAP = {
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
  BANK: '银行转账'
}

/** 优惠券状态 → 文案 */
export const COUPON_STATUS_MAP = {
  1: '未使用',
  2: '已使用',
  3: '已过期',
  4: '已作废'
}

export const DEFAULT_AVATAR = '/assest/default.jpg'
export const AI_AVATAR = '/assest/number1.png'

/** 购物车规格兜底映射（按商品名关键词匹配） */
export function getDefaultSpecs(goodsName = '') {
  const map = shop.cartSpecsMap || {}
  const hit = Object.keys(map).find((k) => !k.startsWith('_') && goodsName.includes(k))
  return hit ? map[hit] : map._default
}

export function orderStatusText(status) {
  return ORDER_STATUS_MAP[status] ?? '未知状态'
}

export function orderStatusDesc(status) {
  return ORDER_DESC_MAP[status] ?? ''
}

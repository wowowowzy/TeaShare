import { createRouter, createWebHistory } from 'vue-router'

/**
 * 全部懒加载。createWebHistory 的 base 取 Vite 的 BASE_URL（生产为 '/tea/'），
 * 否则子目录部署时刷新任意子路由都会 404。
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: () => import('../views/home/HomeView.vue') },
    { path: '/login', name: 'login', component: () => import('../views/login/LoginView.vue') },
    {
      path: '/tradition',
      name: 'tradition',
      component: () => import('../views/tradition/TraditionView.vue')
    },
    { path: '/master', name: 'master', component: () => import('../views/teacher/MasterView.vue') },
    { path: '/quiz', name: 'quiz', component: () => import('../views/quiz/QuizView.vue') },
    { path: '/shop', name: 'shop', component: () => import('../views/shop/ShopView.vue') },
    {
      path: '/product/:id',
      name: 'product',
      component: () => import('../views/productdetail/ProductDetailView.vue')
    },
    {
      path: '/seckill/:id',
      name: 'seckill',
      component: () => import('../views/seckill/SeckillDetailView.vue')
    },
    { path: '/cart', name: 'cart', component: () => import('../views/cart/CartView.vue') },
    { path: '/payment', name: 'payment', component: () => import('../views/pay/PaymentView.vue') },
    {
      path: '/payment/success',
      name: 'paymentSuccess',
      component: () => import('../views/pay/PaymentSuccessView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/profile/ProfileView.vue')
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/community/CommunityView.vue')
    },
    {
      path: '/community/publish',
      name: 'communityPublish',
      component: () => import('../views/community/PublishView.vue')
    },
    {
      path: '/community/post/:id',
      name: 'communityPost',
      component: () => import('../views/community/PostDetailView.vue')
    },
    {
      path: '/community/my-posts',
      name: 'communityMyPosts',
      component: () => import('../views/community/MyPostsView.vue')
    },
    {
      path: '/order/detail',
      name: 'orderDetail',
      component: () => import('../views/profile/OrderDetailView.vue')
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  }
})

export default router

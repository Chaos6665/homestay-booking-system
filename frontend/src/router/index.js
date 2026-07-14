import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import HomestayList from '../views/HomestayList.vue';
import HomestayDetail from '../views/HomestayDetail.vue';
import MerchantReviews from '../views/MerchantReviews.vue';
import MerchantHomestays from '../views/MerchantHomestays.vue';
import UserOrders from '../views/UserOrders.vue';
import AdminReviews from '../views/AdminReviews.vue';
import AdminUsers from '../views/AdminUsers.vue';
import AdminNotices from '../views/AdminNotices.vue';
import Notice from '../views/Notice.vue';

const routes = [
  {
    path: '/',
    redirect: '/homestays'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/homestays',
    name: 'HomestayList',
    component: HomestayList
  },
  {
    path: '/homestays/:id',
    name: 'HomestayDetail',
    component: HomestayDetail
  },
  {
    path: '/notice',
    name: 'Notice',
    component: Notice
  },
  {
    path: '/merchant/reviews',
    name: 'MerchantReviews',
    component: MerchantReviews
  },
  {
    path: '/merchant/homestays',
    name: 'MerchantHomestays',
    component: MerchantHomestays
  },
  {
    path: '/orders',
    name: 'UserOrders',
    component: UserOrders
  },
  {
    path: '/admin/reviews',
    name: 'AdminReviews',
    component: AdminReviews
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUsers
  },
  {
    path: '/admin/notices',
    name: 'AdminNotices',
    component: AdminNotices
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;

<template>
  <div class="page" v-if="currentUser">
    <div class="header">
      <h2>我的订单</h2>
      <el-button @click="goHomestays">去浏览民宿</el-button>
    </div>

    <el-table :data="list" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="订单ID" width="90" />
      <el-table-column prop="homestayId" label="民宿ID" width="100" />
      <el-table-column prop="checkInDate" label="入住日期" width="120" />
      <el-table-column prop="checkOutDate" label="离店日期" width="120" />
      <el-table-column prop="guestCount" label="人数" width="80" />
      <el-table-column prop="totalAmount" label="金额(元)" width="110" />
      <el-table-column prop="status" label="状态" width="110" />
    </el-table>

    <div v-if="!loading && list.length === 0" class="empty-tip">暂时还没有订单，去下单试试吧。</div>
  </div>
  <div v-else class="page">请先登录后再查看订单。</div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../api/request';

const router = useRouter();
const currentUser = ref(null);
const list = ref([]);
const loading = ref(false);

const goHomestays = () => {
  router.push('/homestays');
};

const loadData = async () => {
  if (!currentUser.value) return;
  loading.value = true;
  try {
    const res = await request.get(`/api/orders/user/${currentUser.value.id}`);
    if (res.code === 0) {
      list.value = res.data || [];
    }
  } catch (e) {
    ElMessage.error('加载订单失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  const userJson = localStorage.getItem('user');
  if (userJson) {
    currentUser.value = JSON.parse(userJson);
  }
  loadData();
});
</script>

<style scoped>
.page {
  padding: 24px;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.empty-tip {
  margin-top: 16px;
  color: #999;
}
</style>

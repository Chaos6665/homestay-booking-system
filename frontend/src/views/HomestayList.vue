<template>
  <div class="page">
    <div class="page-header">
      <h2>民宿列表</h2>
      <div style="margin-left: auto; display: flex; align-items: center">
        <el-button type="primary" @click="goOrders">我的订单</el-button>
      </div>
    </div>
    <div class="search-bar">
      <el-input
        v-model="city"
        placeholder="按城市搜索，如：上海"
        clearable
        style="width: 240px"
      />
      <el-button type="primary" style="margin-left: 12px" @click="onSearch">搜索</el-button>
    </div>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col
        v-for="item in pagedList"
        :key="item.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        class="card-wrapper"
      >
        <el-card class="homestay-card" shadow="hover" @click="goDetail(item.id)">
          <img :src="item.coverImageUrl || defaultImg" class="cover" alt="cover" />
          <div class="info">
            <div class="title">{{ item.title }}</div>
            <div class="meta">{{ item.city }} · 可住 {{ item.maxGuests || 2 }} 人</div>
            <div class="price">￥{{ item.pricePerNight }} / 晚</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination" v-if="list.length > 0">
      <el-pagination
        layout="prev, pager, next"
        :total="list.length"
        :page-size="pageSize"
        v-model:current-page="currentPage"
      />
    </div>

    <div class="footer">
      <div>民宿预约管理系统  1533687577@qq.com</div>
      <div>如有问题请联系平台或商家，祝您旅途愉快～</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import request from '../api/request';

const router = useRouter();
const list = ref([]);
const city = ref('');
const currentPage = ref(1);
const pageSize = 16;
const defaultImg =
  'https://img1.baidu.com/it/u=1907358127,2770897569&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500';

const loadData = async () => {
  const params = {};
  if (city.value) {
    params.city = city.value;
  }
  const res = await request.get('/api/homestays', { params });
  if (res.code === 0) {
    list.value = res.data || [];
    currentPage.value = 1;
  }
};

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  const end = start + pageSize;
  return list.value.slice(start, end);
});

const onSearch = () => {
  loadData();
};

const goDetail = (id) => {
  router.push(`/homestays/${id}`);
};

const goOrders = () => {
  router.push('/orders');
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.search-bar {
  margin-top: 24px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.card-wrapper {
  margin-bottom: 16px;
}

.homestay-card {
  cursor: pointer;
}

.cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 4px;
}

.info {
  margin-top: 8px;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.meta {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

.price {
  margin-top: 8px;
  color: #f56c6c;
  font-weight: 600;
}

.footer {
  margin-top: 40px;
  padding: 16px 24px;
  background-color: #333;
  color: #fff;
  text-align: center;
  font-size: 13px;
}
</style>

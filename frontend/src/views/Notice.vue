<template>
  <div class="page">
    <el-card class="notice-card">
      <h2>公告信息</h2>
      <el-divider />

      <div v-for="item in pagedNotices" :key="item.id" class="notice-item">
        <div class="notice-title">{{ item.title }}</div>
        <div class="notice-content">{{ item.content }}</div>
        <div class="notice-footer">{{ item.date }}</div>
      </div>

      <div class="pagination">
        <span>共 {{ notices.length }} 条</span>
        <el-pagination
          layout="prev, pager, next"
          :total="notices.length"
          :page-size="pageSize"
          v-model:current-page="currentPage"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import request from '../api/request';

const notices = ref([]);

const currentPage = ref(1);
const pageSize = ref(2);

const pagedNotices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return notices.value.slice(start, end);
});

const loadData = async () => {
  try {
    const res = await request.get('/api/notices');
    if (res.code === 0) {
      // 后端按时间倒序返回，前端直接使用
      notices.value = res.data || [];
    }
  } catch (e) {
    ElMessage.error('加载公告失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  padding: 24px;
}

.notice-card {
  max-width: 800px;
  margin: 0 auto;
}

.notice-item + .notice-item {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.notice-content {
  color: #555;
}

.notice-footer {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.pagination {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>

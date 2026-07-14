<template>
  <div class="page" v-if="currentUser && currentUser.role === 'MERCHANT'">
    <div class="header">
      <h2>商家评论管理</h2>
      <el-button type="primary" size="large" style="margin-left: 32px" @click="goHomestays">查看民宿管理</el-button>
    </div>
    <p class="tip">当前商家ID：{{ currentUser.id }}，显示所有属于该商家的民宿评论。</p>

    <el-table :data="list" style="width: 100%" v-loading="loading">
      <el-table-column prop="homestayId" label="民宿ID" width="100" />
      <el-table-column prop="rating" label="评分" width="140">
        <template #default="scope">
          <el-rate v-model="scope.row.rating" disabled show-score score-template="{value} 分" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" min-width="220" />
      <el-table-column prop="createdAt" label="时间" width="160" />
      <el-table-column label="商家回复" min-width="260">
        <template #default="scope">
          <div class="reply-box">
            <div v-if="scope.row.merchantReply" class="reply-text">{{ scope.row.merchantReply }}</div>
            <el-input
              v-model="scope.row._reply"
              type="textarea"
              :rows="2"
              placeholder="输入回复内容"
            />
            <el-button
              type="primary"
              size="small"
              style="margin-top: 4px"
              :loading="scope.row._loading"
              @click="submitReply(scope.row)"
            >回复</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && list.length === 0" class="empty-tip">暂时还没有用户评论。</div>
  </div>
  <div v-else class="page">
    只有商家角色可以访问此页面，请使用商家账号登录。
  </div>
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

const loadData = async () => {
  if (!currentUser.value) return;
  loading.value = true;
  try {
    const res = await request.get(`/api/reviews/merchant/${currentUser.value.id}`);
    if (res.code === 0) {
      list.value = (res.data || []).map((item) => ({
        ...item,
        _reply: item.merchantReply || '',
        _loading: false
      }));
    }
  } catch (e) {
    ElMessage.error('加载评论失败');
  } finally {
    loading.value = false;
  }
};

const goHomestays = () => {
  router.push('/merchant/homestays');
};

const submitReply = async (row) => {
  if (!row._reply) {
    ElMessage.error('回复内容不能为空');
    return;
  }
  row._loading = true;
  try {
    const res = await request.post(`/api/reviews/${row.id}/reply`, {
      merchantReply: row._reply
    });
    if (res.code === 0) {
      ElMessage.success('回复成功');
      row.merchantReply = row._reply;
    } else {
      ElMessage.error(res.msg || '回复失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
  } finally {
    row._loading = false;
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
  margin-bottom: 12px;
}

.tip {
  margin-bottom: 12px;
  color: #666;
}

.reply-box {
  display: flex;
  flex-direction: column;
}

.reply-text {
  margin-bottom: 4px;
  color: #409eff;
}

.empty-tip {
  margin-top: 16px;
  color: #999;
}
</style>

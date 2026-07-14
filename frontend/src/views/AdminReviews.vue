<template>
  <div class="page" v-if="currentUser && currentUser.role === 'ADMIN'">
    <div class="header">
      <h2>评论管理（管理员）</h2>
    </div>

    <el-table :data="list" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="homestayId" label="民宿ID" width="90" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="rating" label="评分" width="140">
        <template #default="scope">
          <el-rate v-model="scope.row.rating" disabled show-score score-template="{value} 分" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" min-width="220" />
      <el-table-column prop="merchantReply" label="商家回复" min-width="220" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="时间" width="160" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-select v-model="scope.row._status" size="small" style="width: 100px; margin-right: 8px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="隐藏" value="HIDDEN" />
          </el-select>
          <el-button size="small" type="primary" @click="updateStatus(scope.row)">保存</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row)" style="margin-left: 4px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && list.length === 0" class="empty-tip">暂时没有评论。</div>
  </div>
  <div v-else class="page">只有管理员角色可以访问此页面，请使用管理员账号登录。</div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../api/request';

const currentUser = ref(null);
const list = ref([]);
const loading = ref(false);

const loadData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/api/reviews');
    if (res.code === 0) {
      list.value = (res.data || []).map((item) => ({
        ...item,
        _status: item.status
      }));
    }
  } catch (e) {
    ElMessage.error('加载评论失败');
  } finally {
    loading.value = false;
  }
};

const updateStatus = async (row) => {
  try {
    const res = await request.put(`/api/reviews/${row.id}`, {
      status: row._status,
      content: row.content
    });
    if (res.code === 0) {
      ElMessage.success('更新成功');
      await loadData();
    } else {
      ElMessage.error(res.msg || '更新失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
  }
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确认要删除这条评论吗？', '提示', { type: 'warning' });
  } catch (e) {
    return;
  }
  try {
    const res = await request.delete(`/api/reviews/${row.id}`);
    if (res.code === 0) {
      ElMessage.success('删除成功');
      await loadData();
    } else {
      ElMessage.error(res.msg || '删除失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
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

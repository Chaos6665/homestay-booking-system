<template>
  <div class="page" v-if="currentUser && currentUser.role === 'ADMIN'">
    <div class="header">
      <h2>用户 / 商家管理</h2>
      <el-select v-model="roleFilter" placeholder="按角色筛选" clearable style="width: 160px" @change="loadData">
        <el-option label="普通用户" value="USER" />
        <el-option label="商家" value="MERCHANT" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-button style="margin-left: 12px" @click="goReviews">评论管理</el-button>
    </div>

    <el-table :data="list" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="scope">
          <el-select v-model="scope.row._role" size="small" style="width: 110px">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="启用" width="90">
        <template #default="scope">
          <el-switch v-model="scope.row._enabled" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button size="small" type="primary" :loading="scope.row._saving" @click="save(scope.row)">
            保存
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && list.length === 0" class="empty-tip">暂无用户数据。</div>
  </div>
  <div v-else class="page">只有管理员角色可以访问此页面，请使用管理员账号登录。</div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../api/request';

const currentUser = ref(null);
const list = ref([]);
const loading = ref(false);
const roleFilter = ref('');
const router = useRouter();

const goReviews = () => {
  router.push('/admin/reviews');
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/api/admin/users', {
      params: roleFilter.value ? { role: roleFilter.value } : {}
    });
    if (res.code === 0) {
      list.value = (res.data || []).map((u) => ({
        ...u,
        _role: u.role,
        _enabled: u.enabled !== false,
        _saving: false
      }));
    }
  } catch (e) {
    ElMessage.error('加载用户失败');
  } finally {
    loading.value = false;
  }
};

const save = async (row) => {
  row._saving = true;
  try {
    const res = await request.put(`/api/admin/users/${row.id}`, {
      role: row._role,
      enabled: row._enabled
    });
    if (res.code === 0) {
      ElMessage.success('保存成功');
      await loadData();
    } else {
      ElMessage.error(res.msg || '保存失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
  } finally {
    row._saving = false;
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

.header h2 {
  margin-right: 16px;
}

.empty-tip {
  margin-top: 16px;
  color: #999;
}
</style>

<template>
  <div class="page" v-if="currentUser && currentUser.role === 'ADMIN'">
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" size="large" style="margin-left: 32px" @click="openDialog()">新增公告</el-button>
    </div>

    <el-table :data="list" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && list.length === 0" class="empty-tip">暂时还没有公告，请先新增。</div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑公告' : '新增公告'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" :rows="4" v-model="form.content" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
  <div v-else class="page">只有管理员角色可以访问此页面，请使用管理员账号登录。</div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../api/request';

const currentUser = ref(null);
const list = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const saving = ref(false);
const editing = ref(false);

const form = reactive({
  id: null,
  title: '',
  content: ''
});

const loadData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/api/notices');
    if (res.code === 0) {
      list.value = res.data || [];
    }
  } catch (e) {
    ElMessage.error('加载公告失败');
  } finally {
    loading.value = false;
  }
};

const openDialog = (row) => {
  if (row) {
    editing.value = true;
    form.id = row.id;
    form.title = row.title;
    form.content = row.content;
  } else {
    editing.value = false;
    form.id = null;
    form.title = '';
    form.content = '';
  }
  dialogVisible.value = true;
};

const save = async () => {
  if (!form.title) {
    ElMessage.error('标题不能为空');
    return;
  }
  if (!form.content) {
    ElMessage.error('内容不能为空');
    return;
  }
  saving.value = true;
  try {
    let res;
    if (editing.value && form.id) {
      res = await request.put(`/api/notices/${form.id}`, {
        title: form.title,
        content: form.content
      });
    } else {
      res = await request.post('/api/notices', {
        title: form.title,
        content: form.content
      });
    }
    if (res.code === 0) {
      ElMessage.success('保存成功');
      dialogVisible.value = false;
      await loadData();
    } else {
      ElMessage.error(res.msg || '保存失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
  } finally {
    saving.value = false;
  }
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认要删除公告「${row.title}」吗？`, '提示', {
      type: 'warning'
    });
  } catch (e) {
    return;
  }
  try {
    const res = await request.delete(`/api/notices/${row.id}`);
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

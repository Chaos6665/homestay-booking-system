<template>
  <div class="page" v-if="currentUser && currentUser.role === 'MERCHANT'">
    <div class="header">
      <h2>商家民宿管理</h2>
      <el-button type="primary" size="large" style="margin-left: 32px" @click="openDialog()">新增民宿</el-button>
      <el-button type="primary" size="large" style="margin-left: 16px" @click="goReviews">查看评论管理</el-button>
    </div>

    <el-table :data="list" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="city" label="城市" width="120" />
      <el-table-column prop="pricePerNight" label="价格(元/晚)" width="140" />
      <el-table-column prop="maxGuests" label="最多人数" width="120" />
      <el-table-column label="封面图" width="160">
        <template #default="scope">
          <img v-if="scope.row.coverImageUrl" :src="scope.row.coverImageUrl" class="cover" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && list.length === 0" class="empty-tip">暂时还没有民宿，请先新增。</div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑民宿' : '新增民宿'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入民宿标题" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="如：上海" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="最多人数">
          <el-input-number v-model="form.maxGuests" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="价格(元/晚)">
          <el-input v-model="form.pricePerNight" placeholder="例如 500" />
        </el-form-item>
        <el-form-item label="封面图URL">
          <el-input v-model="form.coverImageUrl" placeholder="如 /images/homestays/room1.jpg 或完整网络地址" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input type="textarea" :rows="3" v-model="form.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
  <div v-else class="page">只有商家角色可以访问此页面，请使用商家账号登录。</div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../api/request';

const router = useRouter();
const currentUser = ref(null);
const list = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const saving = ref(false);
const editing = ref(false);

const form = reactive({
  id: null,
  title: '',
  city: '',
  address: '',
  maxGuests: 2,
  pricePerNight: '',
  coverImageUrl: '',
  description: ''
});

const goReviews = () => {
  router.push('/merchant/reviews');
};

const loadData = async () => {
  if (!currentUser.value) return;
  loading.value = true;
  try {
    const res = await request.get('/api/merchant/homestays', {
      params: { merchantId: currentUser.value.id }
    });
    if (res.code === 0) {
      list.value = res.data || [];
    }
  } catch (e) {
    ElMessage.error('加载民宿失败');
  } finally {
    loading.value = false;
  }
};

const openDialog = (row) => {
  if (row) {
    editing.value = true;
    form.id = row.id;
    form.title = row.title;
    form.city = row.city;
    form.address = row.address;
    form.maxGuests = row.maxGuests || 2;
    form.pricePerNight = row.pricePerNight;
    form.coverImageUrl = row.coverImageUrl;
    form.description = row.description;
  } else {
    editing.value = false;
    form.id = null;
    form.title = '';
    form.city = '';
    form.address = '';
    form.maxGuests = 2;
    form.pricePerNight = '';
    form.coverImageUrl = '';
    form.description = '';
  }
  dialogVisible.value = true;
};

const save = async () => {
  if (!form.title) {
    ElMessage.error('标题不能为空');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      title: form.title,
      city: form.city,
      address: form.address,
      maxGuests: form.maxGuests,
      pricePerNight: form.pricePerNight ? Number(form.pricePerNight) : 0,
      coverImageUrl: form.coverImageUrl,
      description: form.description
    };
    let res;
    if (editing.value && form.id) {
      res = await request.put(`/api/merchant/homestays/${form.id}?merchantId=${currentUser.value.id}`, payload);
    } else {
      res = await request.post(`/api/merchant/homestays?merchantId=${currentUser.value.id}`, payload);
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
    await ElMessageBox.confirm(`确认要删除民宿「${row.title}」吗？`, '提示', {
      type: 'warning'
    });
  } catch (e) {
    return;
  }
  try {
    const res = await request.delete(`/api/merchant/homestays/${row.id}`, {
      params: { merchantId: currentUser.value.id }
    });
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

.cover {
  width: 72px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}

.empty-tip {
  margin-top: 16px;
  color: #999;
}
</style>

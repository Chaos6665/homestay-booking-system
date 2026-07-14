<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">民宿预约管理系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="register-tip">
        还没有账号？
        <el-link type="primary" @click="openRegister">立即注册</el-link>
      </div>
    </div>

    <el-dialog v-model="registerVisible" title="注册账号" width="420px">
      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="registerForm.nickname" placeholder="可不填" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="registerForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="doRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import request from '../api/request';

const formRef = ref();
const loading = ref(false);

const registerVisible = ref(false);
const registerLoading = ref(false);
const registerForm = reactive({
  username: '',
  password: '',
  phone: '',
  nickname: '',
  role: 'USER'
});

const form = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      const res = await request.post('/api/auth/login', {
        username: form.username,
        password: form.password
      });
      if (res.code === 0) {
        localStorage.setItem('user', JSON.stringify(res.data));
        ElMessage.success('登录成功，角色：' + res.data.role);
        // 根据角色跳转到不同的首页
        if (res.data.role === 'MERCHANT') {
          window.location.href = '/merchant/reviews';
        } else if (res.data.role === 'ADMIN') {
          window.location.href = '/admin/reviews';
        } else {
          window.location.href = '/homestays';
        }
      } else {
        ElMessage.error(res.msg || '登录失败');
      }
    } catch (e) {
      ElMessage.error('请求失败');
    } finally {
      loading.value = false;
    }
  });
};

const openRegister = () => {
  registerVisible.value = true;
};

const doRegister = async () => {
  if (!registerForm.username || !registerForm.password) {
    ElMessage.error('用户名和密码不能为空');
    return;
  }
  registerLoading.value = true;
  try {
    const res = await request.post('/api/auth/register', {
      username: registerForm.username,
      password: registerForm.password,
      phone: registerForm.phone,
      nickname: registerForm.nickname,
      role: registerForm.role
    });
    if (res.code === 0) {
      ElMessage.success('注册成功，请使用该账号登录');
      registerVisible.value = false;
    } else {
      ElMessage.error(res.msg || '注册失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
  } finally {
    registerLoading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #74ebd5 0%, #acb6e5 100%);
}

.login-card {
  width: 380px;
  padding: 32px 28px 24px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

.title {
  text-align: center;
  margin-bottom: 24px;
}

.register-tip {
  margin-top: 8px;
  text-align: right;
  font-size: 13px;
}
</style>

<template>
  <div class="nav-bar">
    <div class="nav-left">
      <div class="logo-title" @click="goHome">
        <img class="logo-img" src="/images/logo/profile.jpg" alt="logo" />
        <span class="title">民宿预约管理系统</span>
      </div>
      <span class="nav-link" @click="goIndex">首页</span>
      <span class="nav-link" @click="goNotice">公告信息</span>
    </div>
    <div class="nav-right">
      <el-dropdown v-if="user" trigger="click">
        <span class="user-trigger">
          <el-avatar size="small">{{ user.username?.charAt(0)?.toUpperCase() }}</el-avatar>
          <span class="name">{{ user.nickname || user.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goUser">用户端首页</el-dropdown-item>
            <el-dropdown-item v-if="user.role === 'MERCHANT'" @click="goMerchant">
              商家端评论管理
            </el-dropdown-item>
            <el-dropdown-item v-if="user.role === 'MERCHANT'" @click="goMerchantHomestays">
              商家端民宿管理
            </el-dropdown-item>
            <el-dropdown-item v-if="user.role === 'ADMIN'" @click="goAdmin">管理员评论管理</el-dropdown-item>
            <el-dropdown-item v-if="user.role === 'ADMIN'" @click="goAdminNotices">公告管理</el-dropdown-item>
            <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button v-else type="primary" size="small" @click="toLogin">登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';

const router = useRouter();

const user = computed(() => {
  const json = localStorage.getItem('user');
  if (!json) return null;
  try {
    return JSON.parse(json);
  } catch (e) {
    return null;
  }
});

const goHome = () => {
  if (user.value?.role === 'MERCHANT') {
    router.push('/merchant/reviews');
  } else if (user.value?.role === 'ADMIN') {
    router.push('/admin/reviews');
  } else {
    router.push('/homestays');
  }
};

const goIndex = () => {
  router.push('/homestays');
};

const goNotice = () => {
  router.push('/notice');
};

const goUser = () => {
  router.push('/homestays');
};

const goMerchant = () => {
  if (user.value?.role !== 'MERCHANT') {
    ElMessage.warning('当前账号不是商家角色');
    return;
  }
  router.push('/merchant/reviews');
};

const goMerchantHomestays = () => {
  if (user.value?.role !== 'MERCHANT') {
    ElMessage.warning('当前账号不是商家角色');
    return;
  }
  router.push('/merchant/homestays');
};

const goAdmin = () => {
  if (user.value?.role !== 'ADMIN') {
    ElMessage.warning('当前账号不是管理员角色');
    return;
  }
  router.push('/admin/reviews');
};

const goAdminNotices = () => {
  if (user.value?.role !== 'ADMIN') {
    ElMessage.warning('当前账号不是管理员角色');
    return;
  }
  router.push('/admin/notices');
};

const logout = () => {
  localStorage.removeItem('user');
  router.push('/login');
};

const toLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.nav-bar {
  height: 56px;
  background-color: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.nav-left {
  display: flex;
  align-items: center;
}

.logo-title {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.nav-center {
  display: flex;
  align-items: center;
  margin-left: 40px;
  gap: 24px;
}

.nav-link {
  font-size: 16px;
  cursor: pointer;
  color: #000;
  margin-left: 90px;
}

.logo-img {
  width: 28px;      /* 可以按你图片效果再调大/调小 */
  height: 28px;
  margin-right: 8px;
  border-radius: 4px;  /* 想要圆角就保留，不要可以删 */
  object-fit: cover;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #000;
}

.nav-right {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  font-size: 15px;
}

.name {
  margin: 0 8px;
}
</style>

<script setup>
import { onMounted } from "vue";
import { useUserStore } from "./store/user";

const userStore = useUserStore();

onMounted(() => {
  // 在页面加载时检查用户登录状态
  if (userStore.isLoggedIn) {
    // 如果用户已登录，可以在这里加载用户信息或执行其他初始化操作
    console.log("用户已登录", userStore.userRole);
  }
});
</script>

<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<style scoped>
.app-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>

<template>
  <div class="company-detail-page">
    <el-page-header @back="goBack" :title="'返回'" :content="'企业详情'" class="page-header" />
    
    <company-detail :company-id="companyId" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import CompanyDetail from '@/components/CompanyDetail.vue';

const route = useRoute();
const router = useRouter();
const companyId = ref(null);

onMounted(() => {
  companyId.value = route.params.id;
  if (!companyId.value) {
    router.push({ name: 'StudentJobs' });
  }
});

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.company-detail-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}
</style> 
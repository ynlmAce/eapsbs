<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo-container">
        <h1 class="logo-text">大学生就业帮扶系统</h1>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-link active">首页</router-link>
        <router-link to="/jobs" class="nav-link">招聘信息</router-link>
        <router-link to="/about" class="nav-link">关于平台</router-link>
      </div>
      <div class="auth-buttons">
        <router-link to="/login" class="btn btn-login">登录</router-link>
        <router-link to="/register" class="btn btn-register">注册</router-link>
      </div>
    </header>

    <!-- 主横幅 -->
    <div class="banner">
      <div class="banner-content">
        <h2 class="banner-title">全国大学生就业服务平台</h2>
        <p class="banner-text">连接学生与企业，提供便捷的求职渠道</p>
        <div class="banner-buttons">
          <router-link to="/register?role=student" class="btn btn-primary">学生注册</router-link>
          <router-link to="/register?role=company" class="btn btn-secondary">企业入驻</router-link>
        </div>
      </div>
    </div>

    <!-- 服务特点 -->
    <section class="features">
      <div class="section-title">
        <h2>平台特点</h2>
      </div>
      <div class="feature-cards">
        <div class="feature-card">
          <div class="feature-icon">
            <i class="fas fa-graduation-cap"></i>
          </div>
          <h3>优质就业信息</h3>
          <p>提供全面、真实的就业信息，帮助大学生找到理想工作</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <i class="fas fa-building"></i>
          </div>
          <h3>企业招聘直达</h3>
          <p>企业直接发布招聘信息，无中间环节，提高招聘效率</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <i class="fas fa-comments"></i>
          </div>
          <h3>一对一沟通</h3>
          <p>学生与企业可直接沟通，提供更便捷的交流方式</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <i class="fas fa-user-tie"></i>
          </div>
          <h3>辅导员指导</h3>
          <p>专业辅导员提供就业指导，为学生职业发展保驾护航</p>
        </div>
      </div>
    </section>

    <!-- 最新岗位 -->
    <section class="latest-jobs">
      <div class="section-title">
        <h2>最新岗位</h2>
        <router-link to="/jobs" class="view-all">查看全部</router-link>
      </div>
      <div class="job-list">
        <div class="job-card" v-for="(job, index) in latestJobs" :key="index">
          <div class="job-header">
            <h3 class="job-title">{{ job.title }}</h3>
            <span class="job-salary">{{ job.salary }}</span>
          </div>
          <div class="company-info">
            <span class="company-name">{{ job.company }}</span>
            <span class="job-location"><i class="fas fa-map-marker-alt"></i> {{ job.location }}</span>
          </div>
          <div class="job-tags">
            <span class="tag" v-for="(tag, idx) in job.tags" :key="idx">{{ tag }}</span>
          </div>
          <div class="job-footer">
            <span class="post-date">{{ job.date }}</span>
            <router-link :to="`/jobs/${job.id}`" class="btn btn-sm">查看详情</router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="statistics">
      <div class="stat-item">
        <div class="stat-number">10,000+</div>
        <div class="stat-label">累计入驻企业</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">50,000+</div>
        <div class="stat-label">招聘岗位</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">100,000+</div>
        <div class="stat-label">注册学生</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">20,000+</div>
        <div class="stat-label">成功就业</div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h3>关于平台</h3>
          <p>大学生就业帮扶系统是为大学生和企业搭建的桥梁，旨在促进大学生顺利就业。</p>
        </div>
        <div class="footer-section">
          <h3>联系我们</h3>
          <p><i class="fas fa-envelope"></i> contact@employment.edu.cn</p>
          <p><i class="fas fa-phone"></i> 400-123-4567</p>
        </div>
        <div class="footer-section">
          <h3>快速链接</h3>
          <ul>
            <li><router-link to="/about">关于我们</router-link></li>
            <li><router-link to="/help">使用帮助</router-link></li>
            <li><router-link to="/privacy">隐私政策</router-link></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2025 大学生就业帮扶系统 版权所有</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟最新岗位数据
const latestJobs = ref([
  {
    id: 1,
    title: '前端开发工程师',
    salary: '15K-20K',
    company: '科技有限公司',
    location: '北京',
    tags: ['本科', '经验1-3年', '互联网'],
    date: '2025-04-22'
  },
  {
    id: 2,
    title: '市场营销专员',
    salary: '10K-15K',
    company: '营销传媒集团',
    location: '上海',
    tags: ['本科', '应届生', '市场营销'],
    date: '2025-04-21'
  },
  {
    id: 3,
    title: '算法工程师',
    salary: '25K-35K',
    company: '人工智能科技',
    location: '深圳',
    tags: ['硕士', '经验3-5年', 'AI'],
    date: '2025-04-20'
  },
  {
    id: 4,
    title: '人力资源助理',
    salary: '8K-12K',
    company: '人力资源公司',
    location: '广州',
    tags: ['本科', '应届生', 'HR'],
    date: '2025-04-19'
  }
])
</script>

<style scoped>
.home-container {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  color: #333;
}

/* 顶部导航栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 5%;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo-text {
  font-size: 1.5rem;
  color: #1976d2;
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 1.5rem;
}

.nav-link {
  text-decoration: none;
  color: #555;
  font-weight: 500;
  padding: 0.5rem 0;
  position: relative;
}

.nav-link.active, .nav-link:hover {
  color: #1976d2;
}

.nav-link.active::after, .nav-link:hover::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #1976d2;
}

.auth-buttons {
  display: flex;
  gap: 1rem;
}

.btn {
  text-decoration: none;
  padding: 0.5rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
  text-align: center;
  transition: all 0.3s ease;
}

.btn-login {
  color: #1976d2;
  border: 1px solid #1976d2;
  background-color: transparent;
}

.btn-login:hover {
  background-color: rgba(25, 118, 210, 0.1);
}

.btn-register, .btn-primary {
  color: white;
  background-color: #1976d2;
  border: 1px solid #1976d2;
}

.btn-register:hover, .btn-primary:hover {
  background-color: #1565c0;
}

.btn-secondary {
  color: white;
  background-color: #26a69a;
  border: 1px solid #26a69a;
}

.btn-secondary:hover {
  background-color: #00897b;
}

.btn-sm {
  padding: 0.25rem 0.75rem;
  font-size: 0.875rem;
}

/* 主横幅 */
.banner {
  background-image: linear-gradient(135deg, rgba(25, 118, 210, 0.9), rgba(15, 76, 129, 0.9)), url('https://images.pexels.com/photos/1181622/pexels-photo-1181622.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1');
  background-size: cover;
  background-position: center;
  color: white;
  padding: 5rem 5%;
  text-align: center;
}

.banner-title {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.banner-text {
  font-size: 1.25rem;
  margin-bottom: 2rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.banner-buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

/* 服务特点 */
.features {
  padding: 4rem 5%;
  background-color: #f5f5f5;
}

.section-title {
  text-align: center;
  margin-bottom: 3rem;
  position: relative;
}

.section-title h2 {
  font-size: 2rem;
  color: #333;
  display: inline-block;
  position: relative;
}

.section-title h2::after {
  content: '';
  position: absolute;
  width: 50px;
  height: 3px;
  background-color: #1976d2;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
}

.feature-cards {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 2rem;
}

.feature-card {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  flex: 1 1 200px;
  max-width: 280px;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 2.5rem;
  color: #1976d2;
  margin-bottom: 1rem;
}

.feature-card h3 {
  margin-bottom: 1rem;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

/* 最新岗位 */
.latest-jobs {
  padding: 4rem 5%;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-all {
  text-decoration: none;
  color: #1976d2;
  font-weight: 500;
}

.job-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.job-card {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.job-title {
  font-size: 1.25rem;
  margin: 0;
  color: #333;
}

.job-salary {
  color: #e53935;
  font-weight: bold;
}

.company-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.company-name {
  color: #555;
}

.job-location {
  color: #777;
  font-size: 0.875rem;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.tag {
  background-color: #e3f2fd;
  color: #1976d2;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.post-date {
  color: #999;
  font-size: 0.875rem;
}

/* 统计数据 */
.statistics {
  background-color: #1976d2;
  color: white;
  padding: 3rem 5%;
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 2rem;
  text-align: center;
}

.stat-item {
  flex: 1 1 200px;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 1.25rem;
}

/* 页脚 */
.footer {
  background-color: #263238;
  color: #b0bec5;
  padding: 3rem 5% 1rem;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 2rem;
  margin-bottom: 2rem;
}

.footer-section {
  flex: 1 1 250px;
}

.footer-section h3 {
  color: white;
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.footer-section p {
  line-height: 1.6;
  margin-bottom: 0.5rem;
}

.footer-section ul {
  list-style: none;
  padding: 0;
}

.footer-section ul li {
  margin-bottom: 0.5rem;
}

.footer-section a {
  text-decoration: none;
  color: #b0bec5;
  transition: color 0.3s ease;
}

.footer-section a:hover {
  color: white;
}

.footer-bottom {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255,255,255,0.1);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    padding: 1rem;
  }
  
  .logo-container {
    margin-bottom: 1rem;
  }
  
  .nav-links {
    margin-bottom: 1rem;
  }
  
  .banner {
    padding: 3rem 1rem;
  }
  
  .banner-title {
    font-size: 2rem;
  }
  
  .feature-cards {
    gap: 1rem;
  }
  
  .feature-card {
    max-width: 100%;
  }
  
  .job-list {
    grid-template-columns: 1fr;
  }
}
</style> 
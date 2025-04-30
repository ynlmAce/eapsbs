<template>
  <div class="post-detail-page" v-if="post">
    <div class="post-header">
      <h2 class="post-title">{{ post.title }}</h2>
      <div class="post-meta">{{ post.author_name }} · {{ formatDate(post.created_at) }}</div>
    </div>
    <el-button
      :type="liked ? 'success' : 'default'"
      :icon="liked ? 'el-icon-thumb' : 'el-icon-thumb'"
      @click="handleLike"
      :loading="liking"
      style="margin-bottom: 1rem;"
    >
      <template v-if="liked">已点赞（点击取消）</template>
      <template v-else>点赞</template>
      （{{ likeCount }}）
    </el-button>
    <div class="post-content">{{ post.content }}</div>
    <div class="post-actions">
      <el-button v-if="canChat" type="primary" @click="chatWithAuthor">私聊贴主</el-button>
    </div>
    <div class="comments-section">
      <h3>评论</h3>
      <div v-if="loadingComments" class="loading">评论加载中...</div>
      <div v-else>
        <div v-if="comments.length === 0" class="empty">暂无评论</div>
        <div class="comment-list">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <div class="comment-meta">{{ comment.author_name }} · {{ formatDate(comment.created_at) }}</div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
      </div>
      <el-form :model="commentForm" ref="commentFormRef" class="comment-form" @submit.prevent>
        <el-form-item prop="content">
          <el-input v-model="commentForm.content" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="写下你的评论..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitComment">发表评论</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
  <div v-else class="loading">加载中...</div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getForumPostDetail, getForumComments, createForumComment } from '@/api/forum'
import { useUserStore } from '@/store/user'
import { useForumStore } from '@/store/forum'

const route = useRoute()
const router = useRouter()
const post = ref(null)
const comments = ref([])
const loadingComments = ref(true)
const submitting = ref(false)
const commentForm = ref({ content: '' })
const commentFormRef = ref()
const liking = ref(false)

const forumStore = useForumStore()

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.slice(0, 16).replace('T', ' ')
}

const loadPost = async () => {
  const id = route.params.id
  try {
    const res = await getForumPostDetail(id)
    if (res && res.body) {
      const raw = res.body
      post.value = {
        ...raw,
        author_id: raw.author_id !== undefined ? raw.author_id : (raw.authorId !== undefined ? raw.authorId : ''),
        author_name: raw.author_name || raw.authorName || '',
        comment_count: raw.comment_count !== undefined ? raw.comment_count : (raw.commentCount !== undefined ? raw.commentCount : 0),
        like_count: raw.like_count !== undefined ? raw.like_count : (raw.likeCount !== undefined ? raw.likeCount : 0),
        created_at: raw.created_at || raw.createdAt || '',
        updated_at: raw.updated_at || raw.updatedAt || '',
        is_deleted: raw.is_deleted !== undefined ? raw.is_deleted : (raw.isDeleted !== undefined ? raw.isDeleted : 0),
      }
      // 初始化 store 的点赞数和状态
      forumStore.setPostLikeCount(post.value.id, post.value.like_count || 0)
      await forumStore.fetchPostLikeStatus(post.value.id)
      return
    }
  } catch (e) {
    post.value = null
  }
}

const loadComments = async () => {
  loadingComments.value = true
  try {
    const res = await getForumComments(route.params.id)
    if (res && res.body && Array.isArray(res.body)) {
      comments.value = res.body
    } else {
      comments.value = []
    }
  } catch (e) {
    comments.value = []
  } finally {
    loadingComments.value = false
  }
}

const submitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  submitting.value = true
  try {
    const res = await createForumComment(route.params.id, { content: commentForm.value.content })
    if (res && res.error === 0) {
      ElMessage.success('评论成功')
      commentForm.value.content = ''
      loadComments()
    } else {
      ElMessage.error(res?.message || '评论失败')
    }
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const canChat = computed(() => post.value && post.value.author_id && post.value.author_id !== currentUserId.value)
const chatWithAuthor = () => {
  router.push({ name: 'StudentChat', query: { userId: post.value.author_id } })
}

const handleLike = async () => {
  if (liking.value || !post.value) return
  liking.value = true
  try {
    // 操作前强制拉取一次后端状态
    await forumStore.fetchPostLikeStatus(post.value.id);
    if (forumStore.postLikeStatus[post.value.id]) {
      await forumStore.unlikePost(post.value.id);
      ElMessage.success('已撤销点赞');
    } else {
      await forumStore.likePost(post.value.id);
      ElMessage.success('点赞成功');
    }
    // 操作后再拉取一次，确保同步
    await forumStore.fetchPostLikeStatus(post.value.id);
  } catch (e) {
    ElMessage.error('操作失败');
    await forumStore.fetchPostLikeStatus(post.value.id);
  } finally {
    liking.value = false
  }
}

const likeCount = computed(() => post.value ? (forumStore.postLikeCount[post.value.id] || 0) : 0)
const liked = computed(() => post.value ? !!forumStore.postLikeStatus[post.value.id] : false)

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped>
.post-detail-page {
  width: 100vw;
  min-width: 360px;
  margin: 0;
  padding: 2.5rem 0 2rem 0;
  background: #f8fafc;
  border-radius: 0;
  box-shadow: none;
}
@media (min-width: 1400px) {
  .post-detail-page {
    max-width: 1300px;
    padding: 3rem 4rem 3rem 4rem;
  }
}
.post-header {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e3e8ee;
}
.post-title {
  font-size: 2rem;
  color: #1976d2;
  margin: 0;
  font-weight: bold;
}
.post-meta {
  color: #999;
  font-size: 1rem;
  margin-top: 0.5rem;
}
.post-content {
  color: #333;
  margin-bottom: 1.5rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 1.2rem 1rem;
  white-space: pre-line;
  font-size: 1.1rem;
}
.post-actions {
  margin-bottom: 2rem;
}
.el-button {
  transition: box-shadow 0.2s, background 0.2s;
}
.el-button:hover {
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.12);
  background: #e3f2fd;
}
.comments-section {
  margin-top: 2.5rem;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.06);
  padding: 2rem 1.5rem 1.5rem 1.5rem;
}
.comments-section h3 {
  font-size: 1.2rem;
  color: #1976d2;
  margin-bottom: 1.2rem;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  margin-bottom: 1.5rem;
}
.comment-item {
  background: #f5f8fa;
  border-radius: 8px;
  padding: 1rem 1.2rem;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  box-shadow: 0 1px 4px rgba(25, 118, 210, 0.04);
}
.comment-meta {
  color: #888;
  font-size: 0.98rem;
  margin-bottom: 0.3rem;
  font-weight: 500;
}
.comment-content {
  color: #333;
  font-size: 1.05rem;
  word-break: break-all;
}
.comment-form {
  margin-top: 1.5rem;
  background: #f8fafc;
  border-radius: 8px;
  padding: 1.2rem 1rem 0.5rem 1rem;
  box-shadow: 0 1px 4px rgba(25, 118, 210, 0.04);
}
.el-textarea__inner {
  font-size: 1.05rem;
  min-height: 60px;
}
.loading {
  text-align: center;
  color: #888;
  margin: 2rem 0;
}
.empty {
  text-align: center;
  color: #aaa;
  margin: 2rem 0;
}
</style> 
import { defineStore } from 'pinia'
import { likeForumPost, unlikeForumPost, isForumPostLiked, getForumPostDetail } from '@/api/forum'

export const useForumStore = defineStore('forum', {
  state: () => ({
    postLikeStatus: {}, // { [postId]: true/false }
    postLikeCount: {},  // { [postId]: number }
  }),
  actions: {
    async likePost(postId) {
      await likeForumPost(postId)
      this.postLikeStatus[postId] = true
      const detail = await getForumPostDetail(postId)
      this.postLikeCount[postId] = detail?.body?.like_count || 0
    },
    async unlikePost(postId) {
      await unlikeForumPost(postId)
      this.postLikeStatus[postId] = false
      const detail = await getForumPostDetail(postId)
      this.postLikeCount[postId] = detail?.body?.like_count || 0
    },
    setPostLikeCount(postId, count) {
      this.postLikeCount[postId] = count
    },
    setPostLiked(postId, liked) {
      this.postLikeStatus[postId] = liked
    },
    async fetchPostLikeStatus(postId) {
      try {
        const res = await isForumPostLiked(postId)
        this.postLikeStatus[postId] = !!(res && typeof res.body === 'boolean' ? res.body : false)
      } catch {
        this.postLikeStatus[postId] = false
      }
    }
  }
}) 
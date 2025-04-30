import request from './axios'
import { callApi } from '@/utils/apiUtils'

// 获取帖子列表
export function getForumPosts(params) {
  return request({
    url: '/api/forum/posts',
    method: 'get',
    params
  })
}

// 获取帖子详情
export function getForumPostDetail(id) {
  return request({
    url: `/api/forum/posts/${id}`,
    method: 'get'
  })
}

// 发布新帖子
export function createForumPost(data) {
  return callApi(
    request({
      url: '/api/forum/posts',
      method: 'post',
      data: data || {}
    }),
    {
      showLoading: true,
      showSuccess: true,
      successMsg: '发帖成功',
      errorMsg: '发帖失败'
    }
  )
}

// 获取评论列表
export function getForumComments(postId) {
  return request({
    url: `/api/forum/posts/${postId}/comments`,
    method: 'get'
  })
}

// 发布评论
export function createForumComment(postId, data) {
  return request({
    url: `/api/forum/posts/${postId}/comments`,
    method: 'post',
    data
  })
} 

// 帖子点赞
export function likeForumPost(postId) {
  return request({
    url: `/api/forum/posts/${postId}/like`,
    method: 'post'
  })
}

// 查询当前用户是否已点赞
export function isForumPostLiked(postId) {
  return request({
    url: `/api/forum/posts/${postId}/liked`,
    method: 'get'
  })
}

// 撤销点赞
export function unlikeForumPost(postId) {
  return request({
    url: `/api/forum/posts/${postId}/unlike`,
    method: 'post'
  })
} 
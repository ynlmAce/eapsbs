package com.bs.eaps.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bs.eaps.entity.ForumPost;
import com.bs.eaps.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.bs.eaps.security.JwtUserDetails;
import com.bs.eaps.entity.ForumPostLike;
import com.bs.eaps.service.ForumPostLikeService;

@RestController
@RequestMapping("/api/forum/posts")
public class ForumPostController {
    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumPostLikeService forumPostLikeService;

    // 分页获取帖子列表
    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ForumPost> postPage = forumPostService.page(
                new Page<>(page, pageSize),
                new QueryWrapper<ForumPost>().eq("is_deleted", 0).orderByDesc("created_at"));
        Map<String, Object> res = new HashMap<>();
        res.put("error", 0);
        res.put("body", postPage.getRecords());
        res.put("message", "");
        return res;
    }

    // 获取单个帖子详情
    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        ForumPost post = forumPostService.getById(id);
        Map<String, Object> res = new HashMap<>();
        if (post == null || post.getIsDeleted() != 0) {
            res.put("error", 404);
            res.put("body", null);
            res.put("message", "帖子不存在");
        } else {
            res.put("error", 0);
            res.put("body", post);
            res.put("message", "");
        }
        return res;
    }

    // 发布新帖子
    @PostMapping
    public Map<String, Object> create(@RequestBody ForumPost post) {
        System.out.println("收到发帖请求: " + post);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = null;
        String username = null;
        if (principal instanceof com.bs.eaps.security.JwtUserDetails) {
            com.bs.eaps.security.JwtUserDetails userDetails = (com.bs.eaps.security.JwtUserDetails) principal;
            userId = userDetails.getId();
            username = userDetails.getUsername();
        } else if (principal instanceof com.bs.eaps.entity.User) {
            com.bs.eaps.entity.User user = (com.bs.eaps.entity.User) principal;
            userId = user.getId();
            username = user.getUsername();
        } else {
            throw new RuntimeException("无法识别的用户类型");
        }
        post.setAuthorId(userId);
        post.setAuthorName(username);
        post.setIsDeleted(0);
        post.setCommentCount(0);
        post.setLikeCount(0);
        boolean saved = forumPostService.save(post);
        Map<String, Object> res = new HashMap<>();
        res.put("error", saved ? 0 : 500);
        res.put("body", saved);
        res.put("message", saved ? "" : "发帖失败");
        return res;
    }

    // 点赞（防重复）
    @PostMapping("/{id}/like")
    public Map<String, Object> like(@PathVariable Long id) {
        Map<String, Object> res = new HashMap<>();
        ForumPost post = forumPostService.getById(id);
        if (post == null || post.getIsDeleted() != 0) {
            res.put("error", 404);
            res.put("body", null);
            res.put("message", "帖子不存在");
            return res;
        }
        Long userId = getCurrentUserId();
        if (forumPostLikeService.exists(id, userId)) {
            res.put("error", 1);
            res.put("body", null);
            res.put("message", "已点赞");
            return res;
        }
        ForumPostLike like = new ForumPostLike();
        like.setPostId(id);
        like.setUserId(userId);
        like.setCreatedAt(new java.util.Date());
        forumPostLikeService.save(like);
        post.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
        forumPostService.updateById(post);
        res.put("error", 0);
        res.put("body", true);
        res.put("message", "");
        return res;
    }

    // 撤销点赞
    @PostMapping("/{id}/unlike")
    public Map<String, Object> unlike(@PathVariable Long id) {
        Map<String, Object> res = new HashMap<>();
        ForumPost post = forumPostService.getById(id);
        if (post == null || post.getIsDeleted() != 0) {
            res.put("error", 404);
            res.put("body", null);
            res.put("message", "帖子不存在");
            return res;
        }
        Long userId = getCurrentUserId();
        if (!forumPostLikeService.exists(id, userId)) {
            res.put("error", 1);
            res.put("body", null);
            res.put("message", "未点赞");
            return res;
        }
        forumPostLikeService.removeByPostIdAndUserId(id, userId);
        post.setLikeCount(Math.max(0, (post.getLikeCount() == null ? 0 : post.getLikeCount()) - 1));
        forumPostService.updateById(post);
        res.put("error", 0);
        res.put("body", true);
        res.put("message", "");
        return res;
    }

    // 查询是否已点赞
    @GetMapping("/{id}/liked")
    public Map<String, Object> liked(@PathVariable Long id) {
        Map<String, Object> res = new HashMap<>();
        Long userId = getCurrentUserId();
        boolean liked = forumPostLikeService.exists(id, userId);
        res.put("error", 0);
        res.put("body", liked);
        res.put("message", "");
        return res;
    }

    // 获取当前用户ID
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.bs.eaps.security.JwtUserDetails) {
            return ((com.bs.eaps.security.JwtUserDetails) principal).getId();
        } else if (principal instanceof com.bs.eaps.entity.User) {
            return ((com.bs.eaps.entity.User) principal).getId();
        } else {
            throw new RuntimeException("无法识别的用户类型");
        }
    }
}
package com.bs.eaps.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bs.eaps.entity.ForumComment;
import com.bs.eaps.entity.ForumPost;
import com.bs.eaps.service.ForumCommentService;
import com.bs.eaps.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.bs.eaps.security.JwtUserDetails;

@RestController
@RequestMapping("/api/forum/posts/{postId}/comments")
public class ForumCommentController {
    @Autowired
    private ForumCommentService forumCommentService;
    @Autowired
    private ForumPostService forumPostService;

    // 获取评论列表
    @GetMapping
    public Map<String, Object> list(@PathVariable Long postId) {
        List<ForumComment> comments = forumCommentService.list(
                new QueryWrapper<ForumComment>().eq("post_id", postId).eq("is_deleted", 0).orderByAsc("created_at"));
        Map<String, Object> res = new HashMap<>();
        res.put("error", 0);
        res.put("body", comments);
        res.put("message", "");
        return res;
    }

    // 发表评论
    @PostMapping
    public Map<String, Object> create(@PathVariable Long postId, @RequestBody ForumComment comment) {
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
        comment.setPostId(postId);
        comment.setAuthorId(userId);
        comment.setAuthorName(username);
        comment.setIsDeleted(0);
        boolean saved = forumCommentService.save(comment);
        // 评论数+1
        if (saved) {
            forumPostService.update().eq("id", postId).setSql("comment_count = comment_count + 1").update();
        }
        Map<String, Object> res = new HashMap<>();
        res.put("error", saved ? 0 : 500);
        res.put("body", saved);
        res.put("message", saved ? "" : "评论失败");
        return res;
    }
}
package com.bs.eaps.utils;

import com.bs.eaps.entity.User;
import com.bs.eaps.security.JwtUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类 - 旧版，保留供参考
 */
@Slf4j
public class SecurityUtilsOrig {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                System.err.println("获取用户ID失败: 用户未认证");
                return null;
            }

            Object principal = authentication.getPrincipal();
            if (principal == null) {
                System.err.println("获取用户ID失败: Principal为空");
                return null;
            }

            // 根据实际认证对象类型获取用户ID
            if (principal instanceof JwtUserDetails) {
                // 从JwtUserDetails中获取用户ID
                JwtUserDetails userDetails = (JwtUserDetails) principal;
                return userDetails.getId();
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) principal;
                return Long.parseLong(user.getUsername());
            } else if (principal instanceof String) {
                // 某些情况下可能直接存储用户ID字符串
                return Long.parseLong((String) principal);
            } else if (principal instanceof User) {
                // 处理User实体类型
                User user = (User) principal;
                return user.getId();
            } else {
                // 输出实际类型以便调试
                System.err.println("获取用户ID失败: Principal类型不支持: " + principal.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            System.err.println("获取当前用户ID时发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            log.info("getCurrentUsername: 认证主体类型={}",
                    principal != null ? principal.getClass().getName() : "null");

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                log.info("从UserDetails获取用户名: {}", username);
                return username;
            } else if (principal instanceof String) {
                log.info("从String类型Principal直接获取用户名: {}", principal);
                return (String) principal;
            } else {
                log.info("无法从Principal获取用户名: {}", principal);
            }
        } else {
            log.info("getCurrentUsername: 用户未登录或未认证");
        }
        return null;
    }

    /**
     * 获取当前登录用户角色
     *
     * @return 角色，如果未登录则返回null
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse(null);
            log.info("获取当前用户角色: {}", role);
            return role;
        }
        log.info("getCurrentUserRole: 用户未登录或未认证");
        return null;
    }

    /**
     * 获取认证信息
     *
     * @return 认证信息
     */
    public static Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("获取认证信息: {}", auth);
        return auth;
    }
}
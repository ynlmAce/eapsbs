package com.bs.eaps.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bs.eaps.security.JwtUserDetails;

import java.util.Map;

/**
 * 安全工具类，提供获取当前登录用户信息的方法
 */
public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID，如果未找到则返回null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            log.debug("当前用户认证主体: {}, 类型: {}", principal, principal != null ? principal.getClass().getName() : "null");

            // 从JwtUserDetails中获取ID
            if (principal instanceof JwtUserDetails) {
                JwtUserDetails userDetails = (JwtUserDetails) principal;
                Long userId = userDetails.getId();
                log.debug("从JwtUserDetails获取到用户ID: {}", userId);
                return userId;
            }

            // 从additionalInfo中获取userId
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                String username = userDetails.getUsername();
                log.debug("当前登录用户名: {}", username);

                // 如果是我们的项目特定实现，可能在Authentication的details中存储了userId
                if (authentication.getDetails() instanceof Map) {
                    try {
                        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
                        if (details.containsKey("userId")) {
                            Long userId = Long.valueOf(details.get("userId").toString());
                            log.debug("从Authentication.details中获取到用户ID: {}", userId);
                            return userId;
                        }
                    } catch (Exception e) {
                        log.warn("从Authentication.details中解析userId失败", e);
                    }
                }
            }

            // 如果principal直接是一个数字，可能就是userId
            try {
                if (principal instanceof Number) {
                    return ((Number) principal).longValue();
                } else if (principal instanceof String && ((String) principal).matches("\\d+")) {
                    return Long.parseLong((String) principal);
                }
            } catch (Exception e) {
                log.warn("将principal转换为userId失败", e);
            }

            log.warn("无法从认证对象中获取用户ID，认证主体: {}", principal);
        } else {
            log.warn("用户未认证");
        }

        return null;
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 用户名，如果未找到则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            }

            return principal.toString();
        }

        return null;
    }

    /**
     * 判断当前用户是否已认证
     * 
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
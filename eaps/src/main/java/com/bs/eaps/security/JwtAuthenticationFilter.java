package com.bs.eaps.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.Enumeration;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 记录请求URI，帮助诊断
            log.info("处理请求: {} {}", request.getMethod(), request.getRequestURI());

            // 记录请求头信息，帮助诊断认证问题
            logRequestHeaders(request);

            // 支持多种token请求头方式
            String jwtToken = extractToken(request);
            if (jwtToken != null) {
                log.info("检测到JWT令牌: {}", jwtToken.substring(0, Math.min(10, jwtToken.length())) + "...");

                try {
                    // 从JWT中提取用户名
                    String username = jwtUtil.extractUsername(jwtToken);
                    log.info("JWT令牌中的用户名: {}", username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 加载用户详情
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        log.info("加载用户详情成功: {}, 角色: {}",
                                userDetails.getUsername(),
                                userDetails.getAuthorities());

                        // 验证令牌是否有效
                        if (jwtUtil.validateToken(jwtToken, userDetails)) {
                            // 创建认证令牌并设置到安全上下文
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            log.info("认证成功，用户: {} 已登录", username);
                        } else {
                            log.warn("JWT令牌验证失败，用户: {}", username);
                        }
                    } else {
                        if (username == null) {
                            log.warn("JWT令牌中没有用户名");
                        }
                        if (SecurityContextHolder.getContext().getAuthentication() != null) {
                            log.info("安全上下文中已存在认证信息");
                        }
                    }
                } catch (ExpiredJwtException e) {
                    log.error("解析JWT令牌失败: {}", e.getMessage(), e);
                    // 明确返回401状态码，告知前端令牌已过期
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":401,\"message\":\"令牌已过期，请重新登录\",\"body\":null}");
                    return; // 终止过滤器链
                } catch (Exception e) {
                    log.error("解析JWT令牌失败: {}", e.getMessage(), e);
                }
            } else {
                log.info("请求中未找到JWT令牌");
            }
        } catch (Exception e) {
            log.error("JWT认证过程中发生异常: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);

        // 记录认证结果
        log.info("过滤器处理完成，认证状态: {}",
                SecurityContextHolder.getContext().getAuthentication() != null ? "已认证" : "未认证");
    }

    /**
     * 从请求中提取JWT令牌，支持多种请求头格式
     */
    private String extractToken(HttpServletRequest request) {
        // 尝试从多个可能的头中获取token
        String[] possibleHeaders = { "auth", "Authorization", "X-Auth-Token" };

        for (String headerName : possibleHeaders) {
            String token = request.getHeader(headerName);

            if (StringUtils.hasText(token)) {
                // 如果token以"Bearer "开头，则移除前缀
                if (token.startsWith("Bearer ")) {
                    log.info("从请求头 {} 中提取到Bearer格式的token", headerName);
                    token = token.substring(7);
                } else {
                    log.info("从请求头 {} 中提取到普通格式的token", headerName);
                }
                return token;
            }
        }

        log.info("请求中未找到有效的JWT令牌");
        return null;
    }

    /**
     * 记录请求头信息，帮助诊断认证问题
     */
    private void logRequestHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder("请求头信息:");
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // 敏感头信息只打印部分
            if (headerName.equalsIgnoreCase("Authorization") ||
                    headerName.equalsIgnoreCase("auth") ||
                    headerName.equalsIgnoreCase("X-Auth-Token")) {
                if (headerValue != null && headerValue.length() > 10) {
                    headerValue = headerValue.substring(0, 10) + "...";
                }
            }

            headers.append("\n  ").append(headerName).append(": ").append(headerValue);
        }

        log.info(headers.toString());
    }
}
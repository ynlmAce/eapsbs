package com.bs.eaps.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
            @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 获取请求头中的JWT令牌（从auth头获取）
            String jwtToken = request.getHeader("auth");

            // 如果未提供令牌，则直接放行
            if (jwtToken == null || jwtToken.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从令牌中提取用户名
            String username = null;
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT令牌已过期: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.warn("JWT令牌格式错误: {}", e.getMessage());
            } catch (UnsupportedJwtException e) {
                logger.warn("不支持的JWT令牌: {}", e.getMessage());
            } catch (SignatureException e) {
                logger.warn("JWT令牌签名验证失败: {}", e.getMessage());
            } catch (Exception e) {
                logger.warn("JWT令牌处理异常: {}", e.getMessage());
            }

            // 如果成功提取用户名并且当前认证上下文为空
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 验证令牌是否有效
                    if (jwtUtil.validateToken(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        logger.debug("认证成功: {}", username);
                    } else {
                        logger.warn("无效的JWT令牌或用户不匹配: {}", username);
                    }
                } catch (Exception e) {
                    logger.warn("无法加载用户: {}, 原因: {}", new Object[] { username, e.getMessage() });
                }
            }
        } catch (Exception e) {
            logger.error("JWT认证过滤过程中发生异常", e);
        }

        filterChain.doFilter(request, response);
    }
}
package com.bs.eaps.security;

import com.bs.eaps.entity.User;
import com.bs.eaps.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT认证提供者，负责验证用户登录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 处理登录认证
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证结果
     * @throws AuthenticationException 认证异常
     */
    public Authentication authenticate(String username, String password) throws AuthenticationException {
        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.info("用户不存在: {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.info("密码错误: {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 验证用户状态
        if ("DISABLED".equals(user.getStatus())) {
            log.info("用户被禁用: {}", username);
            throw new BadCredentialsException("账号已被禁用，请联系管理员");
        }

        // 设置认证信息
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));

        // 构建额外信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("userId", user.getId());
        additionalInfo.put("role", user.getRole());

        // 生成令牌
        JwtUserDetails userDetails = new JwtUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                additionalInfo);

        // 封装认证成功的token
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities);

        return authenticationToken;
    }

    /**
     * 通过JWT令牌获取认证信息
     *
     * @param token JWT令牌
     * @return 认证结果
     * @throws AuthenticationException 认证异常
     */
    public Authentication getAuthentication(String token) throws AuthenticationException {
        // 从token中提取用户名
        String username = jwtUtil.extractUsername(token);

        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BadCredentialsException("无效的令牌");
        }

        // 验证用户状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new BadCredentialsException("账号已被禁用，请联系管理员");
        }

        // 设置认证信息
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));

        // 获取token中的声明
        Claims claims = jwtUtil.extractClaim(token, claims1 -> claims1);
        Map<String, Object> additionalInfo = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            if (!"sub".equals(entry.getKey()) && !"iat".equals(entry.getKey()) && !"exp".equals(entry.getKey())) {
                additionalInfo.put(entry.getKey(), entry.getValue());
            }
        }

        // 构建用户详情
        JwtUserDetails userDetails = new JwtUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                additionalInfo);

        // 封装认证成功的token
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities);
    }
}
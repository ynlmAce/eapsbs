package com.bs.eaps.security;

import com.bs.eaps.entity.User;
import com.bs.eaps.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义用户详情服务
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 设置权限
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));

        // 设置附加信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("userId", user.getId());
        additionalInfo.put("role", user.getRole());

        // 返回用户详情
        return new JwtUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                additionalInfo);
    }
}
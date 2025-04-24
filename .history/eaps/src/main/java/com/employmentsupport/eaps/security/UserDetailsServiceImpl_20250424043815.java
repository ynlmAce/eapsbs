package com.employmentsupport.eaps.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employmentsupport.eaps.common.BusinessException;
import com.employmentsupport.eaps.common.Constants;
import com.employmentsupport.eaps.entity.User;
import com.employmentsupport.eaps.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义UserDetailsService实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 检查用户状态
        if (!Constants.UserStatus.ACTIVE.equals(user.getStatus())) {
            if (Constants.UserStatus.LOCKED.equals(user.getStatus())) {
                throw new BusinessException("账号已被锁定，请联系管理员");
            } else {
                throw new BusinessException("账号未激活，请先激活账号");
            }
        }

        // 创建UserDetails对象
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())))
                .build();
    }
}
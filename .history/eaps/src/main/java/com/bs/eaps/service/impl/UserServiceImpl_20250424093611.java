package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.common.BusinessException;
import com.bs.eaps.common.Constants;
import com.bs.eaps.dto.LoginRequest;
import com.bs.eaps.dto.RegisterRequest;
import com.bs.eaps.dto.UserInfo;
import com.bs.eaps.entity.CompanyProfile;
import com.bs.eaps.entity.CounselorProfile;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.entity.User;
import com.bs.eaps.mapper.CompanyProfileMapper;
import com.bs.eaps.mapper.CounselorProfileMapper;
import com.bs.eaps.mapper.StudentProfileMapper;
import com.bs.eaps.mapper.UserMapper;
import com.bs.eaps.security.JwtUtil;
import com.bs.eaps.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final CounselorProfileMapper counselorProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public UserInfo login(LoginRequest loginRequest) {
        try {
            // 认证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // 查询用户信息
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, loginRequest.getUsername()));

            // 根据用户角色查询对应的个人资料
            String name = "";
            if (Constants.Role.STUDENT.equals(user.getRole())) {
                StudentProfile profile = studentProfileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
                        .eq(StudentProfile::getUserId, user.getId()));
                name = profile != null ? profile.getName() : "";
            } else if (Constants.Role.COMPANY.equals(user.getRole())) {
                CompanyProfile profile = companyProfileMapper.selectOne(new LambdaQueryWrapper<CompanyProfile>()
                        .eq(CompanyProfile::getUserId, user.getId()));
                name = profile != null ? profile.getName() : "";
            } else if (Constants.Role.COUNSELOR.equals(user.getRole())) {
                CounselorProfile profile = counselorProfileMapper.selectOne(new LambdaQueryWrapper<CounselorProfile>()
                        .eq(CounselorProfile::getUserId, user.getId()));
                name = profile != null ? profile.getName() : "";
            }

            // 生成JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole());
            String token = jwtUtil.generateToken(user.getUsername(), claims);

            // 构建并返回用户信息
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .name(name)
                    .token(token)
                    .build();
        } catch (AuthenticationException e) {
            log.warn("用户登录失败: {}", e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }
    }

    @Override
    @Transactional
    public boolean register(RegisterRequest registerRequest) {
        // 验证密码确认
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerRequest.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        user.setStatus(Constants.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 根据角色创建对应的档案
        if (Constants.Role.STUDENT.equals(registerRequest.getRole())) {
            StudentProfile profile = new StudentProfile();
            profile.setUserId(user.getId());
            profile.setName(registerRequest.getName());
            profile.setContact(registerRequest.getContact());
            profile.setSchool(registerRequest.getSchool());
            profile.setMajor(registerRequest.getMajor());
            profile.setBehaviorScore(100);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            studentProfileMapper.insert(profile);
        } else if (Constants.Role.COMPANY.equals(registerRequest.getRole())) {
            CompanyProfile profile = new CompanyProfile();
            profile.setUserId(user.getId());
            profile.setName(registerRequest.getName());
            profile.setUnifiedSocialCreditCode(registerRequest.getUnifiedSocialCreditCode());
            profile.setHrContact(registerRequest.getContact());
            profile.setCertificationStatus(Constants.CertificationStatus.PENDING);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            companyProfileMapper.insert(profile);
        } else if (Constants.Role.COUNSELOR.equals(registerRequest.getRole())) {
            CounselorProfile profile = new CounselorProfile();
            profile.setUserId(user.getId());
            profile.setName(registerRequest.getName());
            profile.setContact(registerRequest.getContact());
            profile.setEmployeeId(registerRequest.getUsername());
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            counselorProfileMapper.insert(profile);
        }

        return true;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return true;
    }

    @Override
    public UserInfo getUserByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            return null;
        }

        // 根据用户角色查询对应的个人资料
        String name = "";
        if (Constants.Role.STUDENT.equals(user.getRole())) {
            StudentProfile profile = studentProfileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
                    .eq(StudentProfile::getUserId, user.getId()));
            name = profile != null ? profile.getName() : "";
        } else if (Constants.Role.COMPANY.equals(user.getRole())) {
            CompanyProfile profile = companyProfileMapper.selectOne(new LambdaQueryWrapper<CompanyProfile>()
                    .eq(CompanyProfile::getUserId, user.getId()));
            name = profile != null ? profile.getName() : "";
        } else if (Constants.Role.COUNSELOR.equals(user.getRole())) {
            CounselorProfile profile = counselorProfileMapper.selectOne(new LambdaQueryWrapper<CounselorProfile>()
                    .eq(CounselorProfile::getUserId, user.getId()));
            name = profile != null ? profile.getName() : "";
        }

        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .name(name)
                .build();
    }

    @Override
    public UserInfo getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        // 根据用户角色查询对应的个人资料
        String name = "";
        if (Constants.Role.STUDENT.equals(user.getRole())) {
            StudentProfile profile = studentProfileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
                    .eq(StudentProfile::getUserId, user.getId()));
            name = profile != null ? profile.getName() : "";
        } else if (Constants.Role.COMPANY.equals(user.getRole())) {
            CompanyProfile profile = companyProfileMapper.selectOne(new LambdaQueryWrapper<CompanyProfile>()
                    .eq(CompanyProfile::getUserId, user.getId()));
            name = profile != null ? profile.getName() : "";
        } else if (Constants.Role.COUNSELOR.equals(user.getRole())) {
            CounselorProfile profile = counselorProfileMapper.selectOne(new LambdaQueryWrapper<CounselorProfile>()
                    .eq(CounselorProfile::getUserId, user.getId()));
            name = profile != null ? profile.getName() : "";
        }

        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .name(name)
                .build();
    }
}
package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.common.BusinessException;
import com.bs.eaps.common.Constants;
import com.bs.eaps.dto.LoginRequest;
import com.bs.eaps.dto.RegisterRequest;
import com.bs.eaps.dto.UserInfo;
import com.bs.eaps.entity.CompanyProfile;
import com.bs.eaps.entity.CounselorProfile;
import com.bs.eaps.entity.HrContact;
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
import java.time.ZoneId;
import java.util.Date;
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
            log.info("开始处理登录请求: 用户名={}, 角色={}", loginRequest.getUsername(), loginRequest.getRole());

            // 不再使用AuthenticationManager，直接查询用户并验证密码
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, loginRequest.getUsername()));

            // 检查用户是否存在
            if (user == null) {
                log.warn("用户不存在: {}", loginRequest.getUsername());
                throw new BusinessException("用户名或密码错误");
            }

            log.info("用户存在: 用户名={}, 实际角色={}", user.getUsername(), user.getRole());

            // 验证密码 - 暂时使用明文比较
            // if (!passwordEncoder.matches(loginRequest.getPassword(),
            // user.getPasswordHash())) {
            if (!loginRequest.getPassword().equals(user.getPasswordHash())) {
                log.warn("密码不正确: {}", loginRequest.getUsername());
                throw new BusinessException("用户名或密码错误");
            }

            // 检查账号状态
            if (!Constants.UserStatus.ACTIVE.equals(user.getStatus())) {
                log.warn("账号状态异常: {}, 状态: {}", loginRequest.getUsername(), user.getStatus());
                throw new BusinessException("账号状态异常，请联系管理员");
            }

            // 检查角色是否匹配（如果前端传了角色参数）
            if (loginRequest.getRole() != null && !loginRequest.getRole().isEmpty()) {
                log.info("验证角色匹配: 请求角色={}, 用户角色={}", loginRequest.getRole(), user.getRole());

                // 大小写不敏感的角色匹配检查
                String requestRoleUpper = loginRequest.getRole().toUpperCase();
                String userRoleUpper = user.getRole().toUpperCase();

                if (!requestRoleUpper.equals(userRoleUpper)) {
                    log.warn("角色不匹配: {}, 请求角色: {}, 实际角色: {}",
                            loginRequest.getUsername(), loginRequest.getRole(), user.getRole());
                    throw new BusinessException("用户类型不匹配");
                }
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

            // 生成JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole());

            log.info("生成JWT令牌, 包含声明: {}", claims);
            String token = jwtUtil.generateToken(user.getUsername(), claims);

            // 构建并返回用户信息
            UserInfo userInfo = UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .name(name)
                    .token(token)
                    .build();

            log.info("登录成功: {}", userInfo.getUsername());
            return userInfo;
        } catch (BusinessException e) {
            log.warn("业务异常导致登录失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("登录过程中发生错误: {}", e.getMessage(), e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    @Override
    @Transactional
    public boolean register(RegisterRequest registerRequest) {
        // 添加日志记录
        log.info("注册请求详情: {}", registerRequest);

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
        // user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPasswordHash(registerRequest.getPassword()); // 直接使用明文密码
        user.setRole(registerRequest.getRole());
        user.setStatus(Constants.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 设置默认密保问题和答案
        String securityQuestion = registerRequest.getSecurityQuestion();
        String securityAnswer = registerRequest.getSecurityAnswer();

        // 如果前端未提供密保问题和答案，则使用默认值
        if (securityQuestion == null || securityQuestion.isEmpty()) {
            securityQuestion = "您的出生地是？";
        }

        if (securityAnswer == null || securityAnswer.isEmpty()) {
            // 默认使用用户名作为密保答案（仅用于演示，实际应用中不建议这样做）
            securityAnswer = "北京";
        }

        user.setSecurityQuestion(securityQuestion);
        // user.setSecurityAnswerHash(passwordEncoder.encode(securityAnswer));
        user.setSecurityAnswerHash(securityAnswer); // 直接使用明文密保答案

        userMapper.insert(user);

        // 根据角色创建对应的档案
        if (Constants.Role.STUDENT.equals(registerRequest.getRole())) {
            StudentProfile profile = new StudentProfile();
            profile.setUserId(user.getId());
            profile.setName(registerRequest.getName());
            profile.setContact(registerRequest.getContact());
            profile.setSchool(registerRequest.getSchool());
            profile.setMajor(registerRequest.getMajor());
            profile.setGrade("未设置");
            profile.setBehaviorScore(100);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            studentProfileMapper.insert(profile);
        } else if (Constants.Role.COMPANY.equals(registerRequest.getRole())) {
            // 企业类型用户需要提供统一社会信用代码
            if (registerRequest.getUnifiedSocialCreditCode() == null
                    || registerRequest.getUnifiedSocialCreditCode().isEmpty()) {
                log.error("企业注册失败：未提供统一社会信用代码");
                throw new BusinessException("企业注册需要提供统一社会信用代码");
            }

            CompanyProfile profile = new CompanyProfile();
            profile.setUserId(user.getId());
            profile.setName(registerRequest.getName());
            profile.setUnifiedSocialCreditCode(registerRequest.getUnifiedSocialCreditCode());

            // 将HR联系方式初始设置为空对象实例，而不是null
            profile.setHrContact(new HrContact());

            profile.setCertificationStatus("pending");
            profile.setIndustry("未设置");
            profile.setSize("未设置");
            profile.setAddress("未设置");
            profile.setCreatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            profile.setUpdatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

            log.info("尝试创建企业档案：userId={}, name={}, code={}, certificationStatus={}",
                    user.getId(), profile.getName(), profile.getUnifiedSocialCreditCode(),
                    profile.getCertificationStatus());

            companyProfileMapper.insert(profile);
            log.info("企业档案创建成功：id={}", profile.getId());
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

        // 验证旧密码 - 使用明文比较
        // if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
        if (!oldPassword.equals(user.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新密码 - 使用明文存储
        // user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordHash(newPassword);
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

    @Override
    public String getSecurityQuestion(String username) {
        log.info("获取用户密保问题: {}", username);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new BusinessException("用户不存在");
        }

        if (user.getSecurityQuestion() == null || user.getSecurityQuestion().isEmpty()) {
            log.warn("用户未设置密保问题: {}", username);
            throw new BusinessException("用户未设置密保问题，请联系管理员");
        }

        return user.getSecurityQuestion();
    }

    @Override
    public boolean verifySecurityAnswer(String username, String securityAnswer) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 直接比较明文密保答案，不再使用哈希比较
        // return passwordEncoder.matches(securityAnswer, user.getSecurityAnswerHash());
        return securityAnswer.equals(user.getSecurityAnswerHash());
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String securityAnswer, String newPassword) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密保答案 - 使用明文比较
        // if (!passwordEncoder.matches(securityAnswer, user.getSecurityAnswerHash())) {
        if (!securityAnswer.equals(user.getSecurityAnswerHash())) {
            throw new BusinessException("密保答案错误");
        }

        // 设置新密码 - 使用明文存储
        // user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordHash(newPassword);
        userMapper.updateById(user);

        return true;
    }
}
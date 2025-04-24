package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.common.BusinessException;
import com.bs.eaps.common.Constants;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.entity.StudentResumeFile;
import com.bs.eaps.mapper.StudentProfileMapper;
import com.bs.eaps.mapper.StudentResumeFileMapper;
import com.bs.eaps.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 学生服务实现类
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentProfileMapper studentProfileMapper;
    private final StudentResumeFileMapper studentResumeFileMapper;

    @Value("${file.upload.path:/tmp/uploads}")
    private String fileUploadPath;

    private static final List<String> ALLOWED_RESUME_EXTENSIONS = Arrays.asList("pdf", "docx");

    @Override
    public StudentProfile getStudentProfile(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        // 查询学生基本信息
        StudentProfile profile = studentProfileMapper.selectById(studentId);
        if (profile == null) {
            throw new BusinessException("找不到对应的学生信息");
        }

        // 可以在这里加载学生的教育经历、项目经历、技能标签等信息
        // 这部分需要根据实际的数据模型和业务需求来完善

        return profile;
    }

    @Override
    @Transactional
    public boolean updateStudentProfile(StudentProfile profile) {
        if (profile == null || profile.getId() == null) {
            throw new BusinessException("学生信息不完整");
        }

        // 校验学生资料
        validateStudentProfile(profile);

        // 更新学生基本信息
        int result = studentProfileMapper.updateById(profile);

        // 可以在这里更新学生的教育经历、项目经历、技能标签等关联信息
        // 这部分需要根据实际的数据模型和业务需求来完善

        return result > 0;
    }

    /**
     * 校验学生资料
     *
     * @param profile 学生资料
     */
    private void validateStudentProfile(StudentProfile profile) {
        if (!StringUtils.hasText(profile.getName())) {
            throw new BusinessException("姓名不能为空");
        }

        if (!StringUtils.hasText(profile.getContact())) {
            throw new BusinessException("联系方式不能为空");
        }

        // 可以添加更多的校验逻辑
    }

    @Override
    @Transactional
    public StudentResumeFile uploadStudentResume(Long studentId, MultipartFile file) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException("简历文件不能为空");
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_RESUME_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("不支持的文件格式，仅支持PDF和DOCX格式");
        }

        // 检查文件大小限制（假设限制为10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("文件大小超过限制，最大支持10MB");
        }

        try {
            // 创建上传目录
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String uploadDir = fileUploadPath + "/resume/student_" + studentId + "/" + dateDir;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadPath.resolve(newFilename);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 保存文件信息到数据库
            StudentResumeFile resumeFile = new StudentResumeFile();
            resumeFile.setStudentProfileId(studentId);
            resumeFile.setFileName(originalFilename);
            resumeFile.setFilePath(filePath.toString().replace('\\', '/'));
            resumeFile.setUploadedAt(LocalDateTime.now());

            studentResumeFileMapper.insert(resumeFile);

            return resumeFile;
        } catch (IOException e) {
            throw new BusinessException("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
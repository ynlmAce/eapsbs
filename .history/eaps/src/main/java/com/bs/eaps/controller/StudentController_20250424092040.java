package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.entity.StudentResumeFile;
import com.bs.eaps.service.StudentService;
import com.bs.eaps.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学生模块控制器
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 获取学生个人资料
     */
    @PostMapping("/profile")
    public ApiResponse getStudentProfile() {
        Long studentId = SecurityUtils.getCurrentUserId();
        StudentProfile profile = studentService.getStudentProfile(studentId);
        return ApiResponse.success(profile);
    }

    /**
     * 更新学生个人资料
     */
    @PostMapping("/profile/update")
    public ApiResponse updateStudentProfile(@RequestBody StudentProfile profile) {
        Long studentId = SecurityUtils.getCurrentUserId();
        profile.setId(studentId);
        boolean result = studentService.updateStudentProfile(profile);
        return ApiResponse.success(result);
    }

    /**
     * 上传学生简历文件
     */
    @PostMapping("/resume/upload")
    public ApiResponse uploadStudentResume(MultipartFile file) {
        Long studentId = SecurityUtils.getCurrentUserId();
        StudentResumeFile resumeFile = studentService.uploadStudentResume(studentId, file);
        return ApiResponse.success(resumeFile);
    }
}
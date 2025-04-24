package com.jobhelper.service;

import com.jobhelper.entity.StudentProfile;
import com.jobhelper.entity.StudentResumeFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学生服务接口
 */
public interface StudentService {

    /**
     * 获取学生个人资料
     * 
     * @param studentId 学生ID
     * @return 学生个人资料
     */
    StudentProfile getStudentProfile(Long studentId);

    /**
     * 更新学生个人资料
     * 
     * @param profile 学生个人资料
     * @return 是否更新成功
     */
    boolean updateStudentProfile(StudentProfile profile);

    /**
     * 上传学生简历文件
     * 
     * @param studentId 学生ID
     * @param file      简历文件
     * @return 简历文件信息
     */
    StudentResumeFile uploadStudentResume(Long studentId, MultipartFile file);
}
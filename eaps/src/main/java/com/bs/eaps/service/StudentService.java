package com.bs.eaps.service;

import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.entity.StudentResumeFile;
import com.bs.eaps.entity.StudentStructuredResume;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 学生服务接口
 */
public interface StudentService extends IService<StudentProfile> {

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

    /**
     * 保存学生项目/实习经历
     * 
     * @param studentId          学生ID
     * @param projectExperiences 项目/实习经历列表
     * @return 是否保存成功
     */
    boolean updateProjectExperience(Long studentId, List<Map<String, Object>> projectExperiences);

    /**
     * 保存学生教育经历
     * 
     * @param studentId            学生ID
     * @param educationExperiences 教育经历列表
     * @return 是否保存成功
     */
    boolean updateEducationExperience(Long studentId, List<Map<String, Object>> educationExperiences);

    /**
     * 保存学生技能标签
     * 
     * @param studentId 学生ID
     * @param skillTags 技能标签列表
     * @return 是否保存成功
     */
    boolean updateSkillTags(Long studentId, List<String> skillTags);

    /**
     * 获取学生的技能标签列表
     * 
     * @param studentId 学生ID
     * @return 技能标签列表
     */
    List<String> getStudentSkillTags(Long studentId);

    /**
     * 根据学生ID和简历部分类型获取结构化简历数据
     * 
     * @param studentProfileId 学生档案ID
     * @param sectionType      简历部分类型(project/experience/education等)
     * @return 简历部分数据列表
     */
    List<StudentStructuredResume> getStructuredResumeByType(Long studentProfileId, String sectionType);
}
package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bs.eaps.dto.job.*;
import com.bs.eaps.entity.JobPosting;
import com.bs.eaps.entity.Application;
import com.bs.eaps.entity.Company;
import com.bs.eaps.entity.StudentBlacklist;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.entity.StudentFavorite;
import com.bs.eaps.mapper.JobPostingMapper;
import com.bs.eaps.mapper.ApplicationMapper;
import com.bs.eaps.mapper.CompanyMapper;
import com.bs.eaps.mapper.StudentBlacklistMapper;
import com.bs.eaps.mapper.StudentProfileMapper;
import com.bs.eaps.mapper.StudentFavoriteMapper;
import com.bs.eaps.service.JobService;
import com.bs.eaps.service.JobTagRelationService;
import com.bs.eaps.service.JobWelfareService;
import com.bs.eaps.service.CounselorTaskService;
import com.bs.eaps.common.Constants;
import com.bs.eaps.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;

/**
 * 岗位服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobPostingMapper jobPostingMapper;
    private final ApplicationMapper applicationMapper;
    private final CompanyMapper companyMapper;
    private final StudentBlacklistMapper studentBlacklistMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final StudentFavoriteMapper studentFavoriteMapper;
    private final JobTagRelationService jobTagRelationService;
    private final JobWelfareService jobWelfareService;
    private final CounselorTaskService counselorTaskService;

    @Override
    public Object getJobList(JobQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<JobPosting> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            queryWrapper.like(JobPosting::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(JobPosting::getDescription, queryDTO.getKeyword());
        }

        // 工作地点筛选
        if (queryDTO.getLocation() != null && !queryDTO.getLocation().isEmpty()) {
            queryWrapper.like(JobPosting::getLocation, queryDTO.getLocation());
        }

        // 其他筛选条件
        if (queryDTO.getEducation() != null && !queryDTO.getEducation().isEmpty()) {
            queryWrapper.eq(JobPosting::getEducation, queryDTO.getEducation());
        }

        if (queryDTO.getExperience() != null && !queryDTO.getExperience().isEmpty()) {
            queryWrapper.eq(JobPosting::getExperience, queryDTO.getExperience());
        }

        if (queryDTO.getJobType() != null && !queryDTO.getJobType().isEmpty()) {
            queryWrapper.eq(JobPosting::getJobType, queryDTO.getJobType());
        }

        // 薪资范围筛选
        if (queryDTO.getSalary() != null) {
            // 这里简化处理，实际可能需要更复杂的薪资范围匹配逻辑
            // ...
        }

        // 岗位状态筛选
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            // 根据前端传入的状态进行过滤
            switch (queryDTO.getStatus()) {
                case "recruiting":
                case "active":
                    queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.RECRUITING);
                    break;
                case "pending":
                    queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.PENDING);
                    break;
                case "rejected":
                    queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.REJECTED);
                    break;
                case "ended":
                case "closed":
                    queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.ENDED);
                    break;
                case "draft":
                    queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.DRAFT);
                    break;
                case "all":
                    // 全部状态不需要添加筛选条件
                    break;
                default:
                    // 未知状态默认显示招聘中
                    queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.RECRUITING);
            }
        } else {
            // 没有提供状态参数时，默认只显示招聘中的岗位
            queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.RECRUITING);
        }

        // 有效期内的岗位
        queryWrapper.ge(JobPosting::getValidUntil, LocalDate.now());

        // 创建分页对象
        Page<JobPosting> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 执行分页查询
        Page<JobPosting> jobPage = jobPostingMapper.selectPage(page, queryWrapper);

        // 转换结果
        List<Map<String, Object>> jobList = jobPage.getRecords().stream().map(job -> {
            Map<String, Object> jobMap = new HashMap<>();

            // 获取企业信息
            Company company = companyMapper.selectById(job.getCompanyId());

            jobMap.put("id", job.getId());
            jobMap.put("title", job.getTitle());
            jobMap.put("companyId", job.getCompanyId());
            jobMap.put("companyName", company != null ? company.getName() : "");
            jobMap.put("location", job.getLocation());
            jobMap.put("salary", job.getSalary());
            jobMap.put("education", job.getEducation());
            jobMap.put("experience", job.getExperience());
            jobMap.put("jobType", job.getJobType());
            jobMap.put("publishedAt", job.getPublishedAt());
            jobMap.put("status", job.getStatus());
            jobMap.put("isCertified", company != null
                    && Constants.CertificationStatus.CERTIFIED.equals(company.getCertificationStatus()));

            // 评分信息
            // TODO: 获取企业评分
            jobMap.put("averageRating", 0.0);

            return jobMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", jobPage.getTotal());
        result.put("list", jobList);

        // 添加各状态岗位数量
        try {
            Map<String, Long> statusCounts = new HashMap<>();
            // 获取各状态岗位数量
            statusCounts.put("recruiting", getJobCountByStatus(Constants.JobStatus.RECRUITING));
            statusCounts.put("pending", getJobCountByStatus(Constants.JobStatus.PENDING));
            statusCounts.put("rejected", getJobCountByStatus(Constants.JobStatus.REJECTED));
            statusCounts.put("ended", getJobCountByStatus(Constants.JobStatus.ENDED));
            result.put("statusCounts", statusCounts);
        } catch (Exception e) {
            log.error("获取岗位状态计数失败", e);
        }

        return result;
    }

    @Override
    public Object getJobDetail(Long jobId) {
        // 查询岗位详情
        JobPosting job = jobPostingMapper.selectById(jobId);
        if (job == null) {
            return new HashMap<>();
        }

        // 获取企业信息
        Company company = companyMapper.selectById(job.getCompanyId());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", job.getId());
        result.put("title", job.getTitle());
        result.put("companyId", job.getCompanyId());
        result.put("companyName", company != null ? company.getName() : "");
        result.put("companyLogo", company != null ? company.getLogoPath() : "");
        result.put("description", job.getDescription());
        result.put("requirement", job.getRequirement());
        result.put("location", job.getLocation());
        result.put("salary", job.getSalary());
        result.put("education", job.getEducation());
        result.put("experience", job.getExperience());
        result.put("jobType", job.getJobType());
        result.put("headcount", job.getHeadcount());
        result.put("workTime", job.getWorkTime());
        // JobPosting中没有这些字段，暂时返回空列表
        result.put("welfareTags", java.util.Collections.emptyList());
        result.put("jobTags", java.util.Collections.emptyList());
        result.put("validUntil", job.getValidUntil());
        result.put("contactPerson", job.getContactPerson());
        result.put("contactMethod", job.getShowContact() ? job.getContactMethod() : "");
        result.put("publishedAt", job.getPublishedAt());
        result.put("status", job.getStatus());

        // 企业信息
        Map<String, Object> companyInfo = new HashMap<>();
        if (company != null) {
            companyInfo.put("id", company.getId());
            companyInfo.put("name", company.getName());
            companyInfo.put("industry", company.getIndustry());
            companyInfo.put("size", company.getSize());
            companyInfo.put("isCertified",
                    Constants.CertificationStatus.CERTIFIED.equals(company.getCertificationStatus()));

            // TODO: 获取企业评分
            companyInfo.put("averageRating", 0.0);
        }
        result.put("companyInfo", companyInfo);

        return result;
    }

    @Override
    @Transactional
    public Object publishJob(Long userId, JobPublishDTO jobDTO) {
        // 详细记录userId和jobDTO数据
        log.info("开始处理发布岗位请求: userId={}, jobDTO={}", userId, jobDTO);

        // 首先根据用户ID查询企业信息
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getUserId, userId);
        Company company = companyMapper.selectOne(queryWrapper);

        // 记录查询到的企业信息
        log.info("根据用户ID查询企业信息: userId={}, company={}", userId, company);

        if (company == null) {
            log.error("企业信息不存在，用户ID={}", userId);
            Map<String, Object> result = new HashMap<>();
            result.put("error", "企业信息不存在，请先完善企业资料");
            return result;
        }

        // 创建岗位对象
        JobPosting job = new JobPosting();
        job.setCompanyId(company.getId()); // 使用企业ID而不是用户ID
        job.setTitle(jobDTO.getTitle());
        job.setDescription(jobDTO.getDescription());
        job.setRequirement(jobDTO.getRequirement());
        job.setLocation(jobDTO.getLocation());
        job.setSalary(jobDTO.getSalary());
        job.setEducation(jobDTO.getEducation());
        job.setExperience(jobDTO.getExperience());
        job.setJobType(jobDTO.getJobType());
        job.setHeadcount(jobDTO.getHeadcount());
        job.setWorkTime(jobDTO.getWorkTime());
        job.setValidUntil(jobDTO.getValidUntil());
        job.setContactPerson(jobDTO.getContactPerson());
        job.setContactMethod(jobDTO.getContactMethod());
        job.setShowContact(jobDTO.getShowContact());
        job.setStatus(Constants.JobStatus.PENDING);
        job.setPublishedAt(LocalDateTime.now());
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        // 记录岗位信息
        log.info("准备保存岗位: job={}", job);

        try {
            // 保存岗位
            jobPostingMapper.insert(job);
            log.info("岗位保存成功: jobId={}", job.getId());

            // 处理岗位标签和福利标签
            try {
                // 处理岗位标签关联
                if (jobDTO.getJobTags() != null && !jobDTO.getJobTags().isEmpty()) {
                    log.info("处理岗位标签关联: jobId={}, tags={}", job.getId(), jobDTO.getJobTags());
                    jobTagRelationService.saveJobTagRelations(job.getId(), jobDTO.getJobTags());
                }

                // 处理福利标签关联
                if (jobDTO.getWelfareTags() != null && !jobDTO.getWelfareTags().isEmpty()) {
                    log.info("处理福利标签关联: jobId={}, tags={}", job.getId(), jobDTO.getWelfareTags());
                    jobWelfareService.saveJobWelfareRelations(job.getId(), jobDTO.getWelfareTags());
                }
            } catch (Exception e) {
                log.error("处理标签关联出错", e);
                // 这里可以选择忽略标签错误，不影响岗位保存
            }

            // 创建辅导员审核任务
            String taskTitle = "岗位审核: " + job.getTitle();
            // 查询公司名称
            if (company != null) {
                taskTitle += " - " + company.getName();
            }
            counselorTaskService.createJobAuditTask(job.getId(), taskTitle);
            log.info("已创建岗位审核任务: jobId={}, title={}", job.getId(), taskTitle);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("jobId", job.getId());
            result.put("status", job.getStatus());

            log.info("岗位发布完成: jobId={}, status={}", job.getId(), job.getStatus());
            return result;
        } catch (Exception e) {
            log.error("保存岗位时发生异常", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> publishJob(Long userId, JobDTO jobDTO) {
        // 详细记录userId和jobDTO数据
        log.info("开始处理发布岗位请求(JobDTO版本): userId={}, jobDTO={}", userId, jobDTO);

        try {
            // 创建岗位对象
            JobPosting job = new JobPosting();
            job.setCompanyId(jobDTO.getCompanyId());
            job.setTitle(jobDTO.getTitle());
            job.setDescription(jobDTO.getDescription());
            job.setRequirement(jobDTO.getRequirement());
            job.setLocation(jobDTO.getLocation());
            job.setSalary(jobDTO.getSalary());
            job.setEducation(jobDTO.getEducation());
            job.setExperience(jobDTO.getExperience());
            job.setJobType(jobDTO.getJobType());
            job.setHeadcount(jobDTO.getHeadcount());
            job.setWorkTime(jobDTO.getWorkTime());
            job.setValidUntil(jobDTO.getValidUntil());
            job.setContactPerson(jobDTO.getContactPerson());
            job.setContactMethod(jobDTO.getContactMethod());
            job.setShowContact(jobDTO.getShowContact());
            job.setStatus(Constants.JobStatus.PENDING);
            job.setPublishedAt(LocalDateTime.now());
            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());

            // 记录岗位信息
            log.info("准备保存岗位(JobDTO版本): job={}", job);

            // 保存岗位
            jobPostingMapper.insert(job);
            log.info("岗位保存成功(JobDTO版本): jobId={}", job.getId());

            // 处理岗位标签和福利标签
            try {
                // 处理岗位标签关联
                if (jobDTO.getJobTags() != null && !jobDTO.getJobTags().isEmpty()) {
                    log.info("处理岗位标签关联: jobId={}, tags={}", job.getId(), jobDTO.getJobTags());
                    jobTagRelationService.saveJobTagRelations(job.getId(), jobDTO.getJobTags());
                }

                // 处理福利标签关联
                if (jobDTO.getWelfareTags() != null && !jobDTO.getWelfareTags().isEmpty()) {
                    log.info("处理福利标签关联: jobId={}, tags={}", job.getId(), jobDTO.getWelfareTags());
                    jobWelfareService.saveJobWelfareRelations(job.getId(), jobDTO.getWelfareTags());
                }
            } catch (Exception e) {
                log.error("处理标签关联出错", e);
                // 这里可以选择忽略标签错误，不影响岗位保存
            }

            // 创建辅导员审核任务
            String taskTitle = "岗位审核: " + job.getTitle();
            // 查询公司名称
            Company company = companyMapper.selectById(job.getCompanyId());
            if (company != null) {
                taskTitle += " - " + company.getName();
            }
            counselorTaskService.createJobAuditTask(job.getId(), taskTitle);
            log.info("已创建岗位审核任务: jobId={}, title={}", job.getId(), taskTitle);

            log.info("岗位发布完成(JobDTO版本): jobId={}, status={}", job.getId(), job.getStatus());
            return ApiResponse.success("岗位发布成功，待审核");
        } catch (Exception e) {
            log.error("保存岗位时发生异常(JobDTO版本)", e);
            return ApiResponse.error(500, "岗位发布失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Object applyJob(Long userId, JobApplicationDTO applicationDTO) {
        log.info("学生投递简历: userId={}, jobId={}", userId, applicationDTO.getJobId());

        // 先根据用户ID获取学生档案ID
        LambdaQueryWrapper<StudentProfile> studentQuery = new LambdaQueryWrapper<>();
        studentQuery.eq(StudentProfile::getUserId, userId);
        StudentProfile studentProfile = studentProfileMapper.selectOne(studentQuery);

        if (studentProfile == null) {
            log.error("找不到学生档案信息: userId={}", userId);
            Map<String, Object> result = new HashMap<>();
            result.put("error", "找不到您的学生档案信息，请先完善个人资料");
            return result;
        }

        Long studentId = studentProfile.getId();
        log.info("获取到学生档案ID: userId={}, studentProfileId={}", userId, studentId);

        // 检查是否已经投递过
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getStudentId, studentId)
                .eq(Application::getJobId, applicationDTO.getJobId());

        if (applicationMapper.selectCount(queryWrapper) > 0) {
            log.info("已经投递过该岗位: studentId={}, jobId={}", studentId, applicationDTO.getJobId());
            Map<String, Object> result = new HashMap<>();
            result.put("error", "已经投递过该岗位");
            return result;
        }

        // 获取岗位所属企业信息
        JobPosting job = jobPostingMapper.selectById(applicationDTO.getJobId());
        if (job == null) {
            log.error("岗位不存在: jobId={}", applicationDTO.getJobId());
            Map<String, Object> result = new HashMap<>();
            result.put("error", "岗位不存在");
            return result;
        }

        // 检查学生是否被该企业拉黑
        LambdaQueryWrapper<StudentBlacklist> blacklistQuery = new LambdaQueryWrapper<>();
        blacklistQuery.eq(StudentBlacklist::getCompanyId, job.getCompanyId())
                .eq(StudentBlacklist::getStudentId, studentId);

        if (studentBlacklistMapper.selectCount(blacklistQuery) > 0) {
            log.warn("学生被企业拉黑，不能投递简历: studentId={}, companyId={}, jobId={}",
                    studentId, job.getCompanyId(), applicationDTO.getJobId());
            Map<String, Object> result = new HashMap<>();
            result.put("error", "您已被该企业拉黑，无法投递简历");
            return result;
        }

        // 创建申请对象
        Application application = new Application();
        application.setStudentId(studentId); // 使用学生档案ID而不是用户ID
        application.setJobId(applicationDTO.getJobId());
        application.setResumeId(applicationDTO.getResumeId());
        application.setCoverLetter(applicationDTO.getCoverLetter());
        application.setStatus("pending"); // 使用枚举常量
        application.setAppliedAt(LocalDateTime.now());
        application.setLastUpdatedAt(LocalDateTime.now());

        // 保存申请
        applicationMapper.insert(application);
        log.info("简历投递成功: applicationId={}, studentId={}, jobId={}",
                application.getId(), studentId, applicationDTO.getJobId());

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", application.getStatus());
        result.put("appliedAt", application.getAppliedAt());

        return result;
    }

    @Override
    public Object getStudentApplicationList(Long userId, StudentApplicationQueryDTO queryDTO) {
        log.info("获取学生投递记录请求: userId={}, queryDTO={}", userId, queryDTO);

        // 先根据用户ID获取学生档案ID
        LambdaQueryWrapper<StudentProfile> studentQuery = new LambdaQueryWrapper<>();
        studentQuery.eq(StudentProfile::getUserId, userId);
        StudentProfile studentProfile = studentProfileMapper.selectOne(studentQuery);

        if (studentProfile == null) {
            log.error("找不到学生档案信息: userId={}", userId);
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("list", java.util.Collections.emptyList());
            return result;
        }

        Long studentId = studentProfile.getId();
        log.info("获取到学生档案ID: userId={}, studentProfileId={}", userId, studentId);

        // 创建分页对象
        Page<Map<String, Object>> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 获取状态参数
        String status = "all".equals(queryDTO.getStatus()) ? null : queryDTO.getStatus();

        // 查询投递记录
        Page<Map<String, Object>> resultPage = applicationMapper.selectStudentApplications(page, studentId, status);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("list", resultPage.getRecords());

        return result;
    }

    @Override
    public Object getCompanyApplicationList(Long companyId, CompanyApplicationQueryDTO queryDTO) {
        log.info("获取企业收到的申请: companyId={}, queryDTO={}", companyId, queryDTO);

        // 获取分组方式
        String groupBy = queryDTO.getGroupBy(); // 可能为 "job", "student" 或 null
        log.info("应用分组方式: {}", groupBy);

        if ("job".equals(groupBy)) {
            // 按岗位分组返回
            return getCompanyApplicationsByJob(companyId, queryDTO);
        } else {
            // 默认按学生列表返回
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

            // 获取过滤参数
            Long jobId = queryDTO.getJobId();
            String studentName = queryDTO.getStudentName();
            String status = "all".equals(queryDTO.getStatus()) ? null : queryDTO.getStatus();
            String keyword = queryDTO.getKeyword(); // 关键词搜索

            // 查询企业收到的申请
            Page<Map<String, Object>> resultPage = applicationMapper.selectCompanyApplications(
                    page, companyId, jobId, studentName, status);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", resultPage.getTotal());
            result.put("applications", resultPage.getRecords()); // 注意这里用applications替代list作为key

            return result;
        }
    }

    /**
     * 按岗位分组获取企业收到的申请
     */
    private Object getCompanyApplicationsByJob(Long companyId, CompanyApplicationQueryDTO queryDTO) {
        log.info("按岗位分组获取企业申请: companyId={}", companyId);

        // 1. 首先获取企业的所有岗位
        LambdaQueryWrapper<JobPosting> jobQuery = new LambdaQueryWrapper<>();
        jobQuery.eq(JobPosting::getCompanyId, companyId);

        // 添加岗位筛选条件
        if (queryDTO.getJobStatusFilter() != null && !queryDTO.getJobStatusFilter().isEmpty()) {
            jobQuery.eq(JobPosting::getStatus, queryDTO.getJobStatusFilter());
        }

        // 添加关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            jobQuery.like(JobPosting::getTitle, queryDTO.getKeyword());
        }

        List<JobPosting> jobs = jobPostingMapper.selectList(jobQuery);

        if (jobs.isEmpty()) {
            log.info("企业没有发布岗位, companyId={}", companyId);
            // 返回空的结果
            Map<String, Object> result = new HashMap<>();
            result.put("jobs", Collections.emptyList());
            return result;
        }

        // 2. 获取每个岗位的申请
        List<Map<String, Object>> jobsWithApplications = new ArrayList<>();
        for (JobPosting job : jobs) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("id", job.getId());
            jobMap.put("title", job.getTitle());
            jobMap.put("status", job.getStatus());

            // 查询该岗位的申请数量
            LambdaQueryWrapper<Application> appCountQuery = new LambdaQueryWrapper<>();
            appCountQuery.eq(Application::getJobId, job.getId());
            long applicationCount = applicationMapper.selectCount(appCountQuery);
            jobMap.put("applicationCount", applicationCount);

            // 如果有申请，获取申请列表
            if (applicationCount > 0) {
                // 获取该岗位的所有申请
                LambdaQueryWrapper<Application> appQuery = new LambdaQueryWrapper<>();
                appQuery.eq(Application::getJobId, job.getId());

                // 添加状态筛选
                if (queryDTO.getStatus() != null && !"all".equals(queryDTO.getStatus())) {
                    appQuery.eq(Application::getStatus, queryDTO.getStatus());
                }

                appQuery.orderByDesc(Application::getAppliedAt);

                List<Application> applications = applicationMapper.selectList(appQuery);

                // 获取申请的详细信息（包括学生信息）
                List<Map<String, Object>> applicationDetails = new ArrayList<>();
                for (Application app : applications) {
                    // 获取学生信息
                    StudentProfile student = studentProfileMapper.selectById(app.getStudentId());
                    if (student != null) {
                        Map<String, Object> appDetail = new HashMap<>();
                        appDetail.put("id", app.getId());
                        appDetail.put("studentId", student.getId());
                        appDetail.put("studentName", student.getName());
                        appDetail.put("studentSchool", student.getSchool());
                        appDetail.put("studentMajor", student.getMajor());
                        appDetail.put("studentGrade", student.getGrade());
                        appDetail.put("resumeId", app.getResumeId());
                        appDetail.put("status", app.getStatus());
                        appDetail.put("applyTime", app.getAppliedAt());
                        appDetail.put("lastUpdatedAt", app.getLastUpdatedAt());
                        appDetail.put("hasResume", app.getResumeId() != null);

                        // 检查学生是否被收藏或拉黑
                        LambdaQueryWrapper<StudentFavorite> favoriteQuery = new LambdaQueryWrapper<>();
                        favoriteQuery.eq(StudentFavorite::getCompanyId, companyId)
                                .eq(StudentFavorite::getStudentId, student.getId());
                        appDetail.put("isFavorite", studentFavoriteMapper.selectCount(favoriteQuery) > 0);

                        LambdaQueryWrapper<StudentBlacklist> blacklistQuery = new LambdaQueryWrapper<>();
                        blacklistQuery.eq(StudentBlacklist::getCompanyId, companyId)
                                .eq(StudentBlacklist::getStudentId, student.getId());
                        appDetail.put("isBlacklisted", studentBlacklistMapper.selectCount(blacklistQuery) > 0);

                        applicationDetails.add(appDetail);
                    }
                }

                jobMap.put("applications", applicationDetails);
            } else {
                jobMap.put("applications", Collections.emptyList());
            }

            jobsWithApplications.add(jobMap);
        }

        // 3. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobsWithApplications);
        return result;
    }

    // 辅助方法：获取指定状态的岗位数量
    private long getJobCountByStatus(String status) {
        LambdaQueryWrapper<JobPosting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobPosting::getStatus, status);
        queryWrapper.ge(JobPosting::getValidUntil, LocalDate.now());
        return jobPostingMapper.selectCount(queryWrapper);
    }

    @Override
    public Long getStudentIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 根据用户ID查询学生档案
            LambdaQueryWrapper<StudentProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentProfile::getUserId, userId);
            StudentProfile studentProfile = studentProfileMapper.selectOne(queryWrapper);

            if (studentProfile == null) {
                log.error("未找到学生信息: userId={}", userId);
                return null;
            }

            return studentProfile.getId();
        } catch (Exception e) {
            log.error("获取学生ID失败: userId={}", userId, e);
            return null;
        }
    }
}
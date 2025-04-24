package com.employmentsupport.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employmentsupport.eaps.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 岗位申请Mapper接口
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    /**
     * 查询学生的申请记录
     * 
     * @param page      分页参数
     * @param studentId 学生ID
     * @param status    状态
     * @return 申请记录
     */
    Page<Map<String, Object>> selectStudentApplications(
            Page<Map<String, Object>> page,
            @Param("studentId") Long studentId,
            @Param("status") String status);

    /**
     * 查询企业收到的申请
     * 
     * @param page        分页参数
     * @param companyId   企业ID
     * @param jobId       岗位ID
     * @param studentName 学生姓名
     * @param status      状态
     * @return 申请记录
     */
    Page<Map<String, Object>> selectCompanyApplications(
            Page<Map<String, Object>> page,
            @Param("companyId") Long companyId,
            @Param("jobId") Long jobId,
            @Param("studentName") String studentName,
            @Param("status") String status);
}
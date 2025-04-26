package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.StudentStructuredResume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学生结构化简历Mapper接口
 */
@Mapper
public interface StudentStructuredResumeMapper extends BaseMapper<StudentStructuredResume> {

    /**
     * 根据学生ID和简历部分类型删除简历数据
     * 
     * @param studentProfileId 学生档案ID
     * @param sectionType      简历部分类型
     * @return 影响行数
     */
    int deleteByStudentIdAndType(@Param("studentProfileId") Long studentProfileId,
            @Param("sectionType") String sectionType);
}
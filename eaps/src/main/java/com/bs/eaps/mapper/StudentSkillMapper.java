package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.StudentSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学生技能关联Mapper接口
 */
@Mapper
public interface StudentSkillMapper extends BaseMapper<StudentSkill> {

    /**
     * 批量插入学生技能关联记录
     * 
     * @param studentProfileId 学生档案ID
     * @param skillTagIds      技能标签ID列表
     * @return 影响行数
     */
    int batchInsert(@Param("studentProfileId") Long studentProfileId,
            @Param("skillTagIds") java.util.List<Long> skillTagIds);

    /**
     * 删除学生的所有技能关联记录
     * 
     * @param studentProfileId 学生档案ID
     * @return 影响行数
     */
    int deleteByStudentId(@Param("studentProfileId") Long studentProfileId);
}
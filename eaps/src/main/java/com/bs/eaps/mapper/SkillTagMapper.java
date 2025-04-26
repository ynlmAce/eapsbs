package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.SkillTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 技能标签Mapper接口
 */
@Mapper
public interface SkillTagMapper extends BaseMapper<SkillTag> {

    /**
     * 获取或创建技能标签
     * 如果标签不存在则创建新标签
     * 
     * @param name 标签名称
     * @return 标签ID
     */
    Long getOrCreateByName(@Param("name") String name);

    /**
     * 获取学生的所有技能标签名称
     * 
     * @param studentProfileId 学生档案ID
     * @return 技能标签名称列表
     */
    List<String> getStudentSkillTags(@Param("studentProfileId") Long studentProfileId);
}
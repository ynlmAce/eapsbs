package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.CounselorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 辅导员档案Mapper
 */
@Mapper
public interface CounselorProfileMapper extends BaseMapper<CounselorProfile> {
    /**
     * 根据userId查找辅导员姓名
     */
    String selectNameByUserId(Long userId);

    /**
     * 根据辅导员档案ID查询关联的用户ID
     */
    @Select("SELECT user_id FROM counselor_profile WHERE id = #{counselorId}")
    Long selectUserIdById(Long counselorId);
}
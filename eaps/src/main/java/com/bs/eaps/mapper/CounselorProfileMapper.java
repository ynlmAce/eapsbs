package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.CounselorProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 辅导员档案Mapper
 */
@Mapper
public interface CounselorProfileMapper extends BaseMapper<CounselorProfile> {
    /**
     * 根据userId查找辅导员姓名
     */
    String selectNameByUserId(Long userId);
}
package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.CompanyProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业档案Mapper接口
 */
@Mapper
public interface CompanyProfileMapper extends BaseMapper<CompanyProfile> {
}
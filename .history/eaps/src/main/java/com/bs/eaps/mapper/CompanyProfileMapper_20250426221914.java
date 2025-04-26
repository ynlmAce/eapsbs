package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.CompanyProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 企业档案Mapper接口
 */
@Mapper
public interface CompanyProfileMapper extends BaseMapper<CompanyProfile> {

    /**
     * 根据企业档案ID查询关联的用户ID
     * 
     * @param companyId 企业档案ID
     * @return 关联的用户ID
     */
    @Select("SELECT user_id FROM company_profile WHERE id = #{companyId}")
    Long selectUserIdByCompanyId(Long companyId);
}
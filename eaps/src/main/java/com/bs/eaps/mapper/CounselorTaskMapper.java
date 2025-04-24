package com.bs.eaps.mapper;

import com.bs.eaps.entity.CounselorTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 辅导员任务Mapper接口
 */
@Mapper
public interface CounselorTaskMapper extends BaseMapper<CounselorTask> {

    /**
     * 统计特定类型的任务数量
     * 
     * @param type 任务类型
     * @return 任务数量
     */
    @Select("SELECT COUNT(*) FROM counselor_task WHERE type = #{type} AND status = 'pending'")
    Integer countByType(@Param("type") String type);

    /**
     * 统计待处理的任务总数
     * 
     * @return 待处理任务总数
     */
    @Select("SELECT COUNT(*) FROM counselor_task WHERE status = 'pending'")
    Integer countPendingTasks();
}
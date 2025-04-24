package com.bs.eaps.mapper;

import com.bs.eaps.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户名查询用户，同findByUsername方法，提供别名
     * 
     * @param username 用户名
     * @return 用户对象
     */
    default User selectByUsername(String username) {
        return findByUsername(username);
    }
}
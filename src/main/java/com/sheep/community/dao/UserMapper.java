package com.sheep.community.dao;

import com.sheep.community.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author sheep
 */

@Mapper
public interface UserMapper {
    User selectById(Integer id);

    User selectByName(String username);

    User selectByEmail(String email);

    Integer insertUser(User user);

    Integer updateStatus(Integer id, Integer status);

    Integer updateHeader(Integer id, String headerUrl);

    Integer updatePassword(Integer id, String password);
}

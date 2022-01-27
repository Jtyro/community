package com.sheep.community.service;

import com.sheep.community.dao.UserMapper;
import com.sheep.community.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sheep
 */
@Service
public class UserService {
    private UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findUserById(Integer id){
        return userMapper.selectById(id);
    }
}

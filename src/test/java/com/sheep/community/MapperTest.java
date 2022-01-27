package com.sheep.community;

import com.sheep.community.dao.DiscussPostMapper;
import com.sheep.community.dao.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper postMapper;

    private String userId;

    @Test
    public void textSelect(){
        System.out.println(userMapper.selectById(101));
        System.out.println(userMapper.selectByName("nowcoder11"));
        System.out.println(userMapper.selectByEmail("nowcoder25@sina.com"));
    }
    @Test
    public void DisText(){
        System.out.println(postMapper.selectDiscussPosts(149,0,10));
        System.out.println(postMapper.selectDiscussPostRows(149));

    }

}

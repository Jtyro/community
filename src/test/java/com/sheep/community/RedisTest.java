package com.sheep.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate<String,Object> template;

    @Test
    public void redis(){
        String redisKey = "test:user";
//        template.opsForValue().set(redisKey,1);
        System.out.println(template.opsForValue().get(redisKey));
    }
}

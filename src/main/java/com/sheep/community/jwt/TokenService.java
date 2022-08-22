package com.sheep.community.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sheep.community.pojo.User;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenService {
    //过期时间五分钟
    private static final long EXPIRE_TIME = 5 * 60 * 1000;

    public String getToken(User user) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        String token = "";
        token = JWT.create().withAudience(String.valueOf(user.getId()))  //将userid保存到token里面
                .withExpiresAt(date) //五分钟后token过期
                .sign(Algorithm.HMAC256(user.getPassword())); // 以 password 作为 token 的密钥
        return token;
    }
}

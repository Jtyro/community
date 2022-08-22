package com.sheep.community.util;

import com.sheep.community.pojo.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替session对象
 * @author sheep
 */
@Component
public class HostHolder {
    private final ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}

package com.sheep.community.controller.interceptor;

import com.sheep.community.pojo.LoginTicket;
import com.sheep.community.pojo.User;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CookieUtil;
import com.sheep.community.util.HostHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author sheep
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        //获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //获取用户信息
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户信息
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        hostHolder.clear();
    }


}

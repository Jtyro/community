package com.sheep.community.controller.interceptor;

import com.sheep.community.pojo.User;
import com.sheep.community.service.MessageService;
import com.sheep.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
            int unreadLetterCount = messageService.findUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnread", unreadLetterCount + unreadNoticeCount);
        }
    }
}

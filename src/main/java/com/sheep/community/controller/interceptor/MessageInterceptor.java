package com.sheep.community.controller.interceptor;

import com.sheep.community.pojo.User;
import com.sheep.community.service.MessageService;
import com.sheep.community.util.HostHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Resource
    private HostHolder hostHolder;
    @Resource
    private MessageService messageService;

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull Object handler, ModelAndView modelAndView) {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
            int unreadLetterCount = messageService.findUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnread", unreadLetterCount + unreadNoticeCount);
        }
    }
}

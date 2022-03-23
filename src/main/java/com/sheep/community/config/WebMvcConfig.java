package com.sheep.community.config;

import com.sheep.community.controller.interceptor.DataInterceptor;
import com.sheep.community.controller.interceptor.LoginRequiredInterceptor;
import com.sheep.community.controller.interceptor.LoginTicketInterceptor;
import com.sheep.community.controller.interceptor.MessageInterceptor;
import com.sheep.community.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author sheep
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private LoginTicketInterceptor loginTicketInterceptor;
//    private LoginRequiredInterceptor loginRequiredInterceptor;
    private MessageInterceptor messageInterceptor;
    @Autowired
    private DataInterceptor dataInterceptor;

    @Autowired
    public void setLoginTicketInterceptor(LoginTicketInterceptor loginTicketInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
    }
//    @Autowired
//    public void setLoginRequiredInterceptor(LoginRequiredInterceptor loginRequiredInterceptor) {
//        this.loginRequiredInterceptor = loginRequiredInterceptor;
//    }
    @Autowired
    public void setMessageInterceptor(MessageInterceptor messageInterceptor) {
        this.messageInterceptor = messageInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}

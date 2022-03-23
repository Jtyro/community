package com.sheep.community.controller;

import com.google.code.kaptcha.Producer;
import com.sheep.community.pojo.User;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author sheep
 */
@Controller
public class LoginController implements CommunityConstant {
    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    private UserService userService;
    private Producer kaptchaProducer;
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    public void setKaptchaProducer(Producer kaptchaProducer) {
        this.kaptchaProducer = kaptchaProducer;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/register")
    public String getRegister() {
        return "site/register";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "site/login";
    }

    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，你的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活过了！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }

    @GetMapping("kaptcha")
    public void kaptcha(HttpServletResponse response) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //将验证码放入redis
        String cookieOwner = CommunityUtil.generateUUID();
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(cookieOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);
        Cookie cookie = new Cookie("cookieOwner",cookieOwner);
        response.addCookie(cookie);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(@CookieValue("cookieOwner") String cookieOwner, String username, String password, String code, boolean remember, Model model, HttpServletResponse response) {
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(cookieOwner);
        String kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        //检查验证码
        if (StringUtils.isBlank(kaptcha)){
            model.addAttribute("codeMsg", "验证码已失效！");
            return "site/login";
        }
        if (StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "site/login";
        }

        //检查账号，密码
        int expiredSeconds = remember ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}

package com.sheep.community.service;

import com.sheep.community.dao.UserMapper;
import com.sheep.community.pojo.LoginTicket;
import com.sheep.community.pojo.User;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.MailClient;
import com.sheep.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author sheep
 */
@Service
public class UserService implements CommunityConstant {
    private UserMapper userMapper;
    private MailClient mailClient;
    private TemplateEngine templateEngine;
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    @Autowired
    public void setMailClient(MailClient mailClient) {
        this.mailClient = mailClient;
    }
    @Autowired
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * 通过Id查找用户
     * @param id id
     * @return 用户
     */
    public User findUserById(Integer id) {
        User user = getCache(id);
        if (user == null){
            user = initCache(id);
        }
        return user;
    }

    /**
     * 通过name查找用户
     */
    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    /**
     * 注册用户
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //处理空值
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMag", "改邮箱已被注册!");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * 用户激活
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //处理空值
        if (StringUtils.isBlank(username)) {
            map.put("usernameMag", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        //验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录
     * @param ticket
     */
    public void logout(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    /**
     * 查找用户凭证
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**
     * 更新用户头像
     * @param userId
     * @param headerUrl
     */
    public void updateHeader(int userId, String headerUrl) {
        userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
    }

    /**
     * 修改用户密码
     */
    public Map<String,Object> updatePassword(int userId, String password, String newPassword){
        Map<String, Object> map = new HashMap<>();
        //处理空值
        if (password == null){
            map.put("passwordMsg","原密码不能为空！");
            return map;
        }
        if (newPassword == null){
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }
        //检查原密码是否正确
        User user = userMapper.selectById(userId);
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","原密码不正确！");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId,newPassword);
        clearCache(userId);

        return map;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }

    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    private User initCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = userMapper.selectById(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    private void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

}

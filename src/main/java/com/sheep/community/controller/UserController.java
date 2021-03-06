package com.sheep.community.controller;

import com.sheep.community.annotation.LoginRequired;
import com.sheep.community.pojo.User;
import com.sheep.community.service.FollowService;
import com.sheep.community.service.LikeService;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author sheep
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private UserService userService;
    private HostHolder hostHolder;
    private LikeService likeService;
    private FollowService followService;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }
    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
    }
    @Autowired
    public void setFollowService(FollowService followService) {
        this.followService = followService;
    }

    @LoginRequired
    @GetMapping("/setting")
    public String getSetting() {
        return "site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadImage(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "???????????????????????????");
            return "site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "???????????????????????????");
            return "site/setting";
        }
        //?????????????????????
        filename = CommunityUtil.generateUUID() + suffix;

        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
            throw new RuntimeException("??????????????????????????????????????????!", e);
        }
        //???????????????????????????WEB?????????
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        filename = uploadPath + "/" + filename;
        //????????????
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(filename)) {
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
        }
    }

    @PostMapping("/password")
    public String updatePwd(String password, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), password, newPassword);
        if (!map.isEmpty()) {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "site/setting";
        }
        return "redirect:/logout";
    }

    @GetMapping("/profile/{userId}")
    public String getProfile(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("??????????????????");
        }
        model.addAttribute("user",user);
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);

        //????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //???????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "site/profile";

    }
}

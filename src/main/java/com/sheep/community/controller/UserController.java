package com.sheep.community.controller;

import com.sheep.community.pojo.Comment;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Page;
import com.sheep.community.pojo.User;
import com.sheep.community.service.*;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sheep
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private LikeService likeService;
    @Resource
    private FollowService followService;
    @Resource
    private DiscussPostService postService;
    @Resource
    private CommentService commentService;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/setting")
    public String getSetting() {
        return "site/setting";
    }


    @PostMapping("/upload")
    public String uploadImage(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        assert filename != null;
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            return "site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;

        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("文件上传失败：" + e.getMessage());
            throw new RuntimeException("文件上传失败，服务器发生异常!", e);
        }
        //更新用户头像路径（WEB路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        filename = uploadPath + "/" + filename;
        //图片后缀
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(filename)) {
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取头像失败：" + e.getMessage());
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
    public String getProfile(@PathVariable("userId") int userId, Model model,
                             @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user", user);
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);

        //关注人数
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝人数
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("orderMode", orderMode);

        return "site/profile";
    }

    @GetMapping("/myPost/{userId}")
    public String getMyPost(@PathVariable("userId") int userId, Model model, Page page,
                            @RequestParam(name = "orderMode", defaultValue = "1") int orderMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }

        page.setRows(postService.findDiscussPostRows(userId));
        page.setPath("/user/myPost/" + userId + "?orderMode=" + orderMode);
        page.setLimit(5);

        List<Map<String, Object>> myPosts = new ArrayList<>();
        List<DiscussPost> discussPosts = postService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                myPosts.add(map);
            }
        }
        model.addAttribute("user", user);
        int discussPostRows = postService.findDiscussPostRows(userId);
        model.addAttribute("postRows", discussPostRows);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("orderMode", orderMode);

        return "site/my-post";
    }

    @GetMapping("/myReply/{userId}")
    public String getMyReply(@PathVariable("userId") int userId, Model model, Page page,
                             @RequestParam(name = "orderMode", defaultValue = "2") int orderMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }

        page.setRows(commentService.findCommentsRows(userId));
        page.setPath("/user/myReply/" + userId + "?orderMode=" + orderMode);
        page.setLimit(5);

        List<Map<String, Object>> myReplies = new ArrayList<>();
        List<Comment> commentList = commentService.findCommentsByUserId(userId, page.getOffset(), page.getLimit());
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                int postId = comment.getEntityId();
                if (comment.getEntityType() != 1) {
                    Comment comment1 = commentService.findCommentById(comment.getEntityId());
                    postId = comment1.getEntityId();
                }
                DiscussPost post = postService.findDiscussPostById(postId);
                map.put("post", post);
                myReplies.add(map);
            }
        }
        int commentsRows = commentService.findCommentsRows(userId);
        model.addAttribute("user", user);
        model.addAttribute("replyRows", commentsRows);
        model.addAttribute("myReplies", myReplies);
        model.addAttribute("orderMode", orderMode);

        return "site/my-reply";
    }
}

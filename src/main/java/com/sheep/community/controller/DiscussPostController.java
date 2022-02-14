package com.sheep.community.controller;

import com.sheep.community.pojo.Comment;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Page;
import com.sheep.community.pojo.User;
import com.sheep.community.service.CommentService;
import com.sheep.community.service.DiscussPostService;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author sheep
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    private HostHolder hostHolder;
    private DiscussPostService postService;
    private UserService userService;
    private CommentService commentService;

    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @Autowired
    public void setPostService(DiscussPostService postService) {
        this.postService = postService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录哦！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setStatus(0);
        post.setType(0);
        post.setCommentCount(0);
        post.setScore(0.0);
        postService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @GetMapping("/detail/{postId}")
    public String getDiscussPostDetail(@PathVariable("postId") int postId, Model model, Page page) {
        //评论
        DiscussPost post = postService.findDiscussPostById(postId);
        model.addAttribute("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment : commentList) {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("comment", comment);
                commentMap.put("user", userService.findUserById(comment.getUserId()));
                //获取回复帖子
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                //回复帖子的处理
                if (replyList != null){
                    for (Comment reply : replyList) {
                        Map<String, Object> replyMap = new HashMap<>();
                        replyMap.put("reply", reply);
                        replyMap.put("user",userService.findUserById(reply.getUserId()));
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyMap.put("target", target);
                        replyVOList.add(replyMap);
                    }
                }
                commentMap.put("replies", replyVOList);
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentMap.put("replyCount", replyCount);
                commentVOList.add(commentMap);
            }
        }
        model.addAttribute("comments", commentVOList);
        return "site/discuss-detail";

    }
}
package com.sheep.community.controller;

import com.sheep.community.event.EventProducer;
import com.sheep.community.pojo.*;
import com.sheep.community.service.CommentService;
import com.sheep.community.service.DiscussPostService;
import com.sheep.community.service.LikeService;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.HostHolder;
import com.sheep.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author sheep
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Resource
    private HostHolder hostHolder;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;
    @Resource
    private CommentService commentService;
    @Resource
    private LikeService likeService;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
        discussPostService.addDiscussPost(post);

        //添加分数变换的帖子
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, post.getId());

        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @GetMapping("/detail/{postId}")
    public String getDiscussPostDetail(@PathVariable("postId") int postId, Model model, Page page) {
        //评论
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        model.addAttribute("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞数量
        long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, postId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        model.addAttribute("likeStatus", hostHolder.getUser() == null ?
                0 : likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, postId));

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("comment", comment);
                commentMap.put("user", userService.findUserById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentMap.put("likeCount", likeCount);
                //点赞状态
                commentMap.put("likeStatus", hostHolder.getUser() == null ?
                        0 : likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId()));
                //获取回复帖子
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                //回复帖子的处理
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyMap = new HashMap<>();
                        replyMap.put("reply", reply);
                        replyMap.put("user", userService.findUserById(reply.getUserId()));
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyMap.put("target", target);
                        //点赞数量
                        likeCount = likeService.findLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyMap.put("likeCount", likeCount);
                        //点赞状态
                        replyMap.put("likeStatus", hostHolder.getUser() == null ?
                                0 : likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId()));

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

    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updatePostType(id, 1);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updatePostStatus(id, 1);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        //添加分数变换的帖子
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updatePostStatus(id, 2);

        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}

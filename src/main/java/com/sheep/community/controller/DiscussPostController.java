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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    private LikeService likeService;
    private EventProducer eventProducer;
    private RedisTemplate redisTemplate;

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

    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
    }

    @Autowired
    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "????????????????????????");
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

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(post.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);


        //???????????????????????????
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, post.getId());

        return CommunityUtil.getJSONString(0, "???????????????");
    }

    @GetMapping("/detail/{postId}")
    public String getDiscussPostDetail(@PathVariable("postId") int postId, Model model, Page page) {
        //??????
        DiscussPost post = postService.findDiscussPostById(postId);
        model.addAttribute("post", post);
        //??????
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //????????????
        long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, postId);
        model.addAttribute("likeCount", likeCount);
        //????????????
        model.addAttribute("likeStatus", hostHolder.getUser() == null ?
                0 : likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, postId));

        //??????????????????
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

                //????????????
                likeCount = likeService.findLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentMap.put("likeCount", likeCount);
                //????????????
                commentMap.put("likeStatus", hostHolder.getUser() == null ?
                        0 : likeService.getLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId()));
                //??????????????????
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                //?????????????????????
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyMap = new HashMap<>();
                        replyMap.put("reply", reply);
                        replyMap.put("user", userService.findUserById(reply.getUserId()));
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyMap.put("target", target);
                        //????????????
                        likeCount = likeService.findLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyMap.put("likeCount", likeCount);
                        //????????????
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
        postService.updatePostType(id, 1);

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
        postService.updatePostStatus(id, 1);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        //???????????????????????????
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        postService.updatePostStatus(id, 2);

        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


}

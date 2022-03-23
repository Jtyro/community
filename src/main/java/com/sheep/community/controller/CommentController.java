package com.sheep.community.controller;

import com.sheep.community.event.EventProducer;
import com.sheep.community.pojo.Comment;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Event;
import com.sheep.community.service.CommentService;
import com.sheep.community.service.DiscussPostService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.HostHolder;
import com.sheep.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author sheep
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    private CommentService commentService;
    private HostHolder hostHolder;
    private EventProducer eventProducer;
    private DiscussPostService postService;
    private RedisTemplate redisTemplate;

    @Autowired
    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }

    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @Autowired
    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @Autowired
    public void setPostService(DiscussPostService postService) {
        this.postService = postService;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/add/{postId}")
    public String addComment(@PathVariable("postId") int postId, Comment comment) {
        comment.setStatus(0);
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setEntityId(comment.getEntityId())
                .setEntityType(comment.getEntityType())
                .setUserId(comment.getUserId())
                .setData("postId", postId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost post = postService.findDiscussPostById(postId);
            event.setEntityUserId(post.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment targetComment = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(targetComment.getUserId());
        }

        eventProducer.fireEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityId(postId)
                    .setEntityType(ENTITY_TYPE_POST);
            eventProducer.fireEvent(event);

            //添加分数变换的帖子
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, postId);
        }
        return "redirect:/discuss/detail/" + postId;
    }

}

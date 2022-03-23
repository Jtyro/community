package com.sheep.community.controller;

import com.sheep.community.event.EventProducer;
import com.sheep.community.pojo.Event;
import com.sheep.community.pojo.User;
import com.sheep.community.service.LikeService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.HostHolder;
import com.sheep.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sheep
 */
@Controller
public class LikeController implements CommunityConstant {
    private LikeService likeService;
    private HostHolder hostHolder;
    private EventProducer eventProducer;
    private RedisTemplate redisTemplate;

    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
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
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId,int postId){
        User user = hostHolder.getUser();
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        Map<String, Object> map = new HashMap<>();
        int likeStatus = likeService.getLikeStatus(user.getId(), entityType, entityId);
        map.put("likeCount", likeService.findLikeCount(entityType,entityId));
        map.put("likeStatus", likeStatus);

        if (likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setUserId(user.getId())
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST){
            //添加分数变换的帖子
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, entityId);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}

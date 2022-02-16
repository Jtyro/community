package com.sheep.community.controller;

import com.sheep.community.pojo.User;
import com.sheep.community.service.LikeService;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.HostHolder;
import com.sheep.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sheep
 */
@Controller
public class LikeController {
    private LikeService likeService;
    private HostHolder hostHolder;

    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
    }
    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId){
        User user = hostHolder.getUser();
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeService.findLikeCount(entityType,entityId));
        map.put("likeStatus", likeService.getLikeStatus(user.getId(),entityType,entityId));

        return CommunityUtil.getJSONString(0,null,map);
    }
}

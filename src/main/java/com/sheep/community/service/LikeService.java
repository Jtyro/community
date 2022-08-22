package com.sheep.community.service;

import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author sheep
 */
@Service
public class LikeService implements CommunityConstant {
    @Resource
    private RedisTemplate redisTemplate;

    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean member = operations.opsForSet().isMember(entityLikeKey, userId);
                operations.multi();
                if (Boolean.TRUE.equals(member)) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    public long findLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Long size = redisTemplate.opsForSet().size(entityLikeKey);
        return size == null ? 0 : size;
    }

    public int getLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (Boolean.TRUE.equals(member)) {
            return LIKE;
        } else {
            return UNLIKE;
        }
    }

    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        return redisTemplate.opsForValue().get(userLikeKey) == null ? 0 : (int) redisTemplate.opsForValue().get(userLikeKey);

    }

}

package com.sheep.community.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sheep.community.dao.DiscussPostMapper;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.util.RedisKeyUtil;
import com.sheep.community.util.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sheep
 */
@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    private final Object lock = new Object();
    @Resource
    private DiscussPostMapper postMapper;
    @Resource
    private SensitiveFilter sensitiveFilter;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //缓存热门帖子
    private LoadingCache<String, List<DiscussPost>> postListCache;
    //缓存帖子数量
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        //初始化postListCache
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    if (key == null || key.length() == 0) {
                        throw new IllegalArgumentException("参数不能为空！");
                    }

                    String[] params = key.split(":");
                    if (params.length != 2) {
                        throw new IllegalArgumentException("参数错误！");
                    }
                    int offset = Integer.parseInt(params[0]);
                    int limit = Integer.parseInt(params[1]);
                    logger.info("load post list from Redis.");

                    //二级缓存
                    String hotKey = RedisKeyUtil.getHotKey(offset, limit);
                    List<DiscussPost> discussPosts = (List<DiscussPost>) redisTemplate.opsForValue().get(hotKey);
                    if(discussPosts != null) return discussPosts;
                    logger.info("load post list from DB.");
                    discussPosts = postMapper.selectDiscussPosts(0, offset, limit, 1);
                    synchronized (lock){
                        if(redisTemplate.opsForValue().get(hotKey) == null){
                            redisTemplate.opsForValue().set(hotKey, discussPosts, 300, TimeUnit.SECONDS);
                        }
                    }
                    return discussPosts;
                });
        //初始化postRowsCache
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(integer -> {
                    logger.info("load post rows from DB.");
                    return postMapper.selectDiscussPostRows(integer);
                });

    }

    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {
            String key = offset + ":" + limit;
            return postListCache.get(key);
        }
        logger.debug("load post list from DB.");
        return postMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows(Integer userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB.");
        return postMapper.selectDiscussPostRows(userId);
    }

    public void addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //过滤转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        postMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int postId) {
        return postMapper.selectDiscussPostById(postId);
    }

    public void updateCommentCount(int id, int commentCount) {
        postMapper.updateCommentCount(id, commentCount);
    }

    public void updatePostType(int id, int type) {
        postMapper.updatePostType(id, type);
    }

    public void updatePostStatus(int id, int status) {
        postMapper.updatePostStatus(id, status);
    }

    public void updatePostScore(int id, double score) {
        postMapper.updatePostScore(id, score);
    }
}

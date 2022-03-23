package com.sheep.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sheep.community.dao.DiscussPostMapper;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sheep
 */
@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    private DiscussPostMapper postMapper;
    private SensitiveFilter sensitiveFilter;

    @Autowired
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }

    @Autowired
    public void setPostMapper(DiscussPostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    private LoadingCache<String, List<DiscussPost>> postListCache;
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        //初始化postListCache
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数不能为空！");
                        }

                        String[] params = key.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);
                        logger.debug("load post list from DB.");
                        return postMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });

        //初始化postRowsCache
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer integer) throws Exception {
                        logger.debug("load post rows from DB.");
                        return postMapper.selectDiscussPostRows(integer);
                    }
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

    public int updatePostType(int id, int type) {
        return postMapper.updatePostType(id, type);
    }

    public int updatePostStatus(int id, int status) {
        return postMapper.updatePostStatus(id, status);
    }

    public int updatePostScore(int id, double score) {
        return postMapper.updatePostScore(id, score);
    }


}

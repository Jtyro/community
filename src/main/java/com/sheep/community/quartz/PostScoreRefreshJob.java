package com.sheep.community.quartz;

import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.service.DiscussPostService;
import com.sheep.community.service.LikeService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
@Component
public class PostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    private static final Date epoch;
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败");
        }
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private LikeService likeService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations<String, Object> operations = redisTemplate.boundSetOps(postScoreKey);
        if (operations.size() == null || operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while (operations.size() != 0) {
            this.refresh((int) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("该帖子不存在 id = " + postId);
            return;
        }
        //是否加精
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, postId);
        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10L + likeCount * 2;
        //计算分数
        double score = Math.log10(Math.max(1, w))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        discussPostService.updatePostScore(postId, score);
    }

}

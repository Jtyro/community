package com.sheep.community.service;

import com.sheep.community.dao.DiscussPostMapper;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author sheep
 */
@Service
public class DiscussPostService {
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

    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit){
        return postMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(Integer userId){
        return postMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if (post == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //过滤转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return postMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int postId){
        return postMapper.selectDiscussPostById(postId);
    }

    public int updateCommentCount(int id, int commentCount){
        return postMapper.updateCommentCount(id, commentCount);
    }
}

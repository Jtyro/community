package com.sheep.community.service;

import com.sheep.community.dao.CommentMapper;
import com.sheep.community.dao.DiscussPostMapper;
import com.sheep.community.pojo.Comment;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author sheep
 */
@Service
public class CommentService implements CommunityConstant {
    private CommentMapper commentMapper;
    private SensitiveFilter sensitiveFilter;
    private DiscussPostMapper postMapper;

    @Autowired
    public void setCommentMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }
    @Autowired
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }
    @Autowired
    public void setPostMapper(DiscussPostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCountByEntity(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int insertComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //过滤内容
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //更新评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            int commentCount = commentMapper.selectCountByEntity(ENTITY_TYPE_POST,comment.getEntityId());
            postMapper.updateCommentCount(comment.getEntityId(),commentCount);
        }
        //添加评论
        return commentMapper.insertComment(comment);
    }
}

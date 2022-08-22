package com.sheep.community.service;

import com.sheep.community.dao.CommentMapper;
import com.sheep.community.pojo.Comment;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author sheep
 */
@Service
public class CommentService implements CommunityConstant {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private SensitiveFilter sensitiveFilter;
    @Resource
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public List<Comment> findCommentsByUserId(int userId, int offset, int limit){
        return commentMapper.selectCommentsByUserId(userId, offset, limit);
    }

    public int findCommentsRows(int userId){
        return commentMapper.selectCommentsRows(userId);
    }

    public int findCountByEntity(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void insertComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //过滤内容
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //更新评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            int commentCount = commentMapper.selectCountByEntity(ENTITY_TYPE_POST,comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),commentCount);
        }
        //添加评论
        commentMapper.insertComment(comment);
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}

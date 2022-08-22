package com.sheep.community.dao;

import com.sheep.community.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author sheep
 */
@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    List<Comment> selectCommentsByUserId(int userId, int offset, int limit);

    int selectCommentsRows(int userId);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}

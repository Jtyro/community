package com.sheep.community.dao;

import com.sheep.community.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author sheep
 */
@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(Integer userId, Integer offset, Integer limit);

    Integer selectDiscussPostRows(@Param("userId") Integer userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);
}

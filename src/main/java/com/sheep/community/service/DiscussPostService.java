package com.sheep.community.service;

import com.sheep.community.dao.DiscussPostMapper;
import com.sheep.community.pojo.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author sheep
 */
@Service
public class DiscussPostService {
    private DiscussPostMapper postMapper;

    @Autowired
    public void setPostMapper(DiscussPostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit){
        return postMapper.selectDiscussPosts(userId, offset, limit);
    }

    public Integer findDiscussPostRows(Integer userId){
        return postMapper.selectDiscussPostRows(userId);
    }
}

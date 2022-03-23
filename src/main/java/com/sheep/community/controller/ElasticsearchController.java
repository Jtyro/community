package com.sheep.community.controller;

import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Page;
import com.sheep.community.service.ElasticsearchService;
import com.sheep.community.service.LikeService;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ElasticsearchController implements CommunityConstant {
    private ElasticsearchService elasticsearchService;
    private UserService userService;
    private LikeService likeService;

    @Autowired
    public void setElasticsearchService(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
    }

    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) throws IOException {
        org.springframework.data.domain.Page<DiscussPost> discussPosts =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null){
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                map.put("user", userService.findUserById(discussPost.getUserId()));
                map.put("likeCount", likeService.findLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                list.add(map);
            }
            model.addAttribute("discussPosts", list);
            model.addAttribute("keyword", keyword);

            page.setPath("/search?keyword=" + keyword);
            page.setLimit(10);

            page.setRows((int) discussPosts.getTotalElements());
        }

        return "site/search";
    }


}

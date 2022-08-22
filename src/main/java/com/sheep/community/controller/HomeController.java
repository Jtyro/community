package com.sheep.community.controller;

import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Page;
import com.sheep.community.pojo.User;
import com.sheep.community.service.DiscussPostService;
import com.sheep.community.service.LikeService;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sheep
 */
@Controller
public class HomeController implements CommunityConstant{

    @Resource
    private UserService userService;
    @Resource
    private DiscussPostService postService;
    @Resource
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        //方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入到Model中
        //所以可以在 Thymeleaf 中直接访问Page里的数据
        page.setRows(postService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        List<DiscussPost> list = postService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                map.put("post", post);
                long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }
}

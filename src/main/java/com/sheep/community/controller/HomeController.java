package com.sheep.community.controller;

import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Page;
import com.sheep.community.pojo.User;
import com.sheep.community.service.DiscussPostService;
import com.sheep.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sheep
 */
@Controller
public class HomeController {
    private UserService userService;
    private DiscussPostService postService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setPostService(DiscussPostService postService) {
        this.postService = postService;
    }

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        //方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入到Model中
        //所以可以在 Thymeleaf 中直接访问Page里的数据
        page.setRows(postService.findDiscussPostRows(0));
        page.setPath("/index");

        List<Map<String,Object>> discussPosts = new ArrayList<>();
        List<DiscussPost> list = postService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        if(list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                map.put("post",post);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }
}

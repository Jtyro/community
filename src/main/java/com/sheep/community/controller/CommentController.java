package com.sheep.community.controller;

import com.sheep.community.pojo.Comment;
import com.sheep.community.service.CommentService;
import com.sheep.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author sheep
 */
@Controller
@RequestMapping("/comment")
public class CommentController {
    private CommentService commentService;
    private HostHolder hostHolder;

    @Autowired
    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }
    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @PostMapping("/add/{postId}")
    public String addComment(@PathVariable("postId") int postId, Comment comment){
        comment.setStatus(0);
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        return "redirect:/discuss/detail/" + postId;
    }

}

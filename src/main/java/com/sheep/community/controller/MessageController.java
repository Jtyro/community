package com.sheep.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.sheep.community.pojo.Message;
import com.sheep.community.pojo.Page;
import com.sheep.community.pojo.User;
import com.sheep.community.service.MessageService;
import com.sheep.community.service.UserService;
import com.sheep.community.util.CommunityConstant;
import com.sheep.community.util.CommunityUtil;
import com.sheep.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author sheep
 */
@Controller
public class MessageController implements CommunityConstant {
    private MessageService messageService;
    private HostHolder hostHolder;
    private UserService userService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Autowired
    public void setHostHolder(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/letter/list")
    public String getConversationList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                int targetId = user.getId().equals(message.getFromId()) ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findUnreadCount(user.getId(), message.getConversationId()));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        int unreadLetterCount = messageService.findUnreadCount(user.getId(), null);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("unreadNoticeCount", unreadNoticeCount);
        return "site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page){
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null){
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target", userService.findUserById(getTargetId(conversationId)));

        //设置已读
        List<Integer> ids = getUnreadId(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";

    }

    private int getTargetId(String conversationId){
        User user = hostHolder.getUser();
        String[] id = conversationId.split("_");
        if (user.getId().equals(Integer.valueOf(id[0]))){
            return Integer.parseInt(id[1]);
        }
        return Integer.parseInt(id[0]);
    }

    private List<Integer> getUnreadId(List<Message> list){
        List<Integer> ids = new ArrayList<>();
        if (list != null){
            for (Message message : list) {
                if (hostHolder.getUser().getId().equals(message.getToId()) && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @PostMapping("/letter/add")
    @ResponseBody
    public String addMessage(String toName, String content){
        User target = userService.findUserByName(toName);
        User user = hostHolder.getUser();
        if (target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        Message message = new Message();
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        message.setFromId(user.getId());
        message.setToId(target.getId());
        if (user.getId() > target.getId()){
            message.setConversationId(target.getId() + "_" + user.getId());
        } else {
            message.setConversationId(user.getId() + "_" + target.getId());
        }

        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    @GetMapping("/notice/list")
    public String listNotice(Model model){
        User user = hostHolder.getUser();
        if (user == null){
            throw new IllegalArgumentException("用户登录信息异常！");
        }


        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", noticeCount);
            int unread = messageService.findUnreadNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
            model.addAttribute("commentNotice", messageVO);
        }



        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", noticeCount);
            int unread = messageService.findUnreadNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);

            model.addAttribute("likeNotice", messageVO);
        }


        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", noticeCount);
            int unread = messageService.findUnreadNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            model.addAttribute("followNotice", messageVO);

        }


        int unreadLetterCount = messageService.findUnreadCount(user.getId(), null);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("unreadNoticeCount", unreadNoticeCount);

        return "site/notice";
    }

    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page){
        User user = hostHolder.getUser();

        page.setPath("/notice/detail/" + topic);
        page.setLimit(5);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> messageList = messageService.findNotice(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> messageVOList = new ArrayList<>();
        if (messageList != null){
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", message);
                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityId", data.get("entityId"));
                map.put("entityType", data.get("entityType"));
                map.put("postId", data.get("postId"));

                map.put("fromUser",userService.findUserById(message.getFromId()));

                messageVOList.add(map);

            }
        }
        model.addAttribute("notices", messageVOList);

        //已读
        List<Integer> ids = getUnreadId(messageList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "site/notice-detail";
    }
}

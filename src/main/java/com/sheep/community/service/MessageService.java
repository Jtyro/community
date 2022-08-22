package com.sheep.community.service;

import com.sheep.community.dao.MessageMapper;
import com.sheep.community.pojo.Message;
import com.sheep.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author sheep
 */
@Service
public class MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findUnreadCount(int userId, String conversationId) {
        return messageMapper.selectUnreadCount(userId, conversationId);
    }

    public void addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        messageMapper.insertMessage(message);
    }

    public void readMessage(List<Integer> ids) {
        messageMapper.updateStatus(ids, 1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findUnreadNoticeCount(int userId, String topic) {
        return messageMapper.selectUnreadNotice(userId, topic);
    }

    public List<Message> findNotice(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotice(userId, topic, offset, limit);
    }
}

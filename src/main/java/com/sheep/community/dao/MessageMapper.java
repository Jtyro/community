package com.sheep.community.dao;

import com.sheep.community.pojo.Comment;
import com.sheep.community.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author sheep
 */
@Mapper
public interface MessageMapper {
    /**
     * 查询与该用户相关的最近的会话记录
     * @param userId 用户Id
     * @param offset 分页起始页
     * @param limit 每页行数
     * @return 会话记录集合
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * 查询与该用户相关的最近的会话记录数
     * @param userId 用户Id
     * @return 会话消息数
     */
    int selectConversationCount(int userId);

    /**
     * 查询该会话记录下的私信记录
     * @param conversationId 会话Id
     * @param offset 分页起始页
     * @param limit 每页行数
     * @return 私信记录集合
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * 查询该会话记录下的私信数
     * @param conversationId 会话Id
     * @return 私信数
     */
    int selectLetterCount(String conversationId);

    /**
     * 按条件查询，若conversationId==null，则查询该用户所有未读消息数
     * 反之，则查询某一会话记录未读消息数
     * @param userId 用户Id
     * @param conversationId 会话Id
     * @return 未读消息数
     */
    int selectUnreadCount(int userId, String conversationId);

    /**
     * 插入消息
     * @param message 消息
     */
    int insertMessage(Message message);

    /**
     * 修改消息状态
     * @param ids id集合
     * @param status 要更改的状态
     */
    int updateStatus(List<Integer> ids, int status);

    Message selectLatestNotice(int userId, String topic);

    int selectNoticeCount(int userId, String topic);

    int selectUnreadNotice(int userId, String topic);

    List<Message> selectNotice(int userId, String topic, int offset, int limit);
}

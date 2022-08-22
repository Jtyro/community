package com.sheep.community.util;

/**
 * @author sheep
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证的超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 事件类型：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 事件类型：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 事件类型：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 事件类型：发布
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 事件类型：删除
     */
    String TOPIC_DELETE = "delete";

    /**
     * 系统通知Id
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 未赞
     */
    int UNLIKE = 0;

    /**
     * 已赞
     */
    int LIKE = 1;
}

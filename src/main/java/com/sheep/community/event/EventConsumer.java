package com.sheep.community.event;

import com.alibaba.fastjson.JSONObject;
import com.sheep.community.dao.elasticsearch.DiscussPostRepository;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Event;
import com.sheep.community.pojo.Message;
import com.sheep.community.service.DiscussPostService;
import com.sheep.community.service.ElasticsearchService;
import com.sheep.community.service.MessageService;
import com.sheep.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private MessageService messageService;
    private DiscussPostService postService;
    private ElasticsearchService elasticsearchService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Autowired
    public void setPostService(DiscussPostService postService) {
        this.postService = postService;
    }
    @Autowired
    public void setElasticsearchService(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null){
            logger.error("消息的格式不正确！");
            return;
        }
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        message.setStatus(0);

        Map<String,Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()){
            content.putAll(event.getData());
        }

        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handleSearch(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null){
            logger.error("消息的格式不正确！");
            return;
        }

        DiscussPost post = postService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDelete(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null){
            logger.error("消息的格式不正确！");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }


}

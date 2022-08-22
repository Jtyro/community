package com.sheep.community.event;

import com.alibaba.fastjson.JSONObject;
import com.sheep.community.pojo.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class EventProducer {
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void fireEvent(Event event){
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}

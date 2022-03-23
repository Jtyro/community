package com.sheep.community.event;

import com.alibaba.fastjson.JSONObject;
import com.sheep.community.pojo.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void fireEvent(Event event){
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}

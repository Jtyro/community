package com.sheep.community.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

@Configuration
public class ESConfig {

    @Bean
    public RestHighLevelClient client(){
        ClientConfiguration configuration = ClientConfiguration.builder()
                .connectedTo("127.0.0.1:9200")
                .build();

        return RestClients.create(configuration).rest();
    }

}

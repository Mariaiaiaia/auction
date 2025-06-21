package com.maria.config;

import com.maria.constant.BidServiceEventConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic createBidTopic(){
        return TopicBuilder
                .name(BidServiceEventConstants.NEW_BID)
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }
}

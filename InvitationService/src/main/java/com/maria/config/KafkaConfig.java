package com.maria.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic createAcceptanceTopic(){
        return TopicBuilder
                .name("acceptance-events")
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

}

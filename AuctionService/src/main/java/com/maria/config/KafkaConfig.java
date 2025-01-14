package com.maria.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic createInvitationTopic(){
        return TopicBuilder
                .name("auction-invitations-events")
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic createNewBidNotificationTopic(){
        return TopicBuilder
                .name("new-bid-notification-events")
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic createAuctionFinishedNotificationTopic(){
        return TopicBuilder
                .name("auction-finished-notification-events")
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic deleteAuctionTopic(){
        return TopicBuilder
                .name("delete-auction-events")
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic userRemovedTopic(){
        return TopicBuilder
                .name("user-removed-notification-events")
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic auctionCreatedTopic(){
        return TopicBuilder
                .name("auction-created-events")
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }
}

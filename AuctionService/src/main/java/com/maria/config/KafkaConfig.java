package com.maria.config;

import com.maria.constant.AuctionServiceEventConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic createInvitationTopic() {
        return TopicBuilder
                .name(AuctionServiceEventConstants.AUCTION_INVITATION)
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic createNewBidNotificationTopic() {
        return TopicBuilder
                .name(AuctionServiceEventConstants.NEW_BID_NOTIFICATION)
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic createAuctionFinishedNotificationTopic() {
        return TopicBuilder
                .name(AuctionServiceEventConstants.AUCTION_FINISHED_NOTIFICATION)
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic deleteAuctionTopic() {
        return TopicBuilder
                .name(AuctionServiceEventConstants.DELETE_AUCTION)
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic auctionCreatedTopic() {
        return TopicBuilder
                .name(AuctionServiceEventConstants.AUCTION_CREATED)
                .replicas(1)
                .partitions(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }
}

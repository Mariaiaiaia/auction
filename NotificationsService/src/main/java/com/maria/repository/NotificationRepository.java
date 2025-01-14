package com.maria.repository;

import com.maria.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
    Flux<Notification> findByUserId(Long userId);
    Flux<Notification> findByAuctionId(Long auctionId);
}

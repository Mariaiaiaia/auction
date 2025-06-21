package com.maria.service;

import com.maria.entity.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Flux<Notification> getNotificationForUser(Long userId);

    Mono<Void> deleteNotificationForUser(String notificationId, Long userId);

    Flux<Void> removeNotificationsByAuction(Long auctionId);

    Mono<Void> deleteNotificationInService(String notificationId);

    Flux<Long> getUserIdForAuctionNotification(Long auctionId);
}

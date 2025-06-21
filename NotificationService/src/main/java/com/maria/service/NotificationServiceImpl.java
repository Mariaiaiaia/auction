package com.maria.service;

import com.maria.constant.NotificationServiceConstants;
import com.maria.entity.Notification;
import com.maria.exception.*;
import com.maria.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final WebClient webClient;
    @Value("${url.bid-service-find-users}")
    private String bidServiceFindUsersUrl;

    @Override
    public Flux<Notification> getNotificationForUser(Long userId) {
        return notificationRepository.findByUserId(userId)
                .switchIfEmpty(Flux.empty());
    }

    @Override
    public Flux<Long> getUserIdForAuctionNotification(Long auctionId) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(bidServiceFindUsersUrl)
                        .build(auctionId))
                .header("X-Internal-Service", "true")
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.error(NotificationServiceConstants.LOG_AUCTION_NOT_FOUNDED, auctionId);
                    return Mono.error(new AuctionNotExistException(NotificationServiceConstants.EX_AUCTION_NOT_EXIST));
                })
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(NotificationServiceConstants.LOG_FAIL_GET_AUCTION, errorBody);
                                    return Mono.error(new NotificationWebClientException(NotificationServiceConstants.EX_SERVICE_ERROR));
                                }))
                .bodyToFlux(Long.class);
    }

    @Override
    public Flux<Void> removeNotificationsByAuction(Long auctionId) {
        return notificationRepository.findByAuctionId(auctionId)
                .flatMap(notification -> deleteNotificationInService(notification.getId()));
    }

    @Override
    public Mono<Void> deleteNotificationInService(String notificationId) {
        return notificationRepository.findById(notificationId)
                .switchIfEmpty(Mono.error(new NotificationNotExistException(NotificationServiceConstants.EX_NOTIFIC_NOT_EXIST)))
                .flatMap(notificationRepository::delete)
                .doOnSuccess(success -> log.info(NotificationServiceConstants.LOG_NOTIFIC_DELETED, notificationId))
                .onErrorResume(ex -> {
                    log.error(NotificationServiceConstants.LOG_FAIL_DELETE_NOTIFIC, ex.getMessage());
                    return Mono.error(new DatabaseOperationException(NotificationServiceConstants.EX_DELETE_NOTIFIC));
                });
    }

    @Override
    public Mono<Void> deleteNotificationForUser(String notificationId, Long userId) {
        return notificationAvailableToInteraction(notificationId, userId)
                .flatMap(notificationRepository::delete)
                .doOnSuccess(success -> log.info(NotificationServiceConstants.LOG_NOTIFIC_DELETED, notificationId))
                .onErrorMap(ex -> {
                    log.error(NotificationServiceConstants.LOG_FAIL_DELETE_NOTIFIC, ex.getMessage());
                    if (ex instanceof NotificationNotExistException || ex instanceof NotificationNotAvailableException) {
                        return ex;
                    }

                    return new DatabaseOperationException(NotificationServiceConstants.EX_DELETE_NOTIFIC);
                });
    }

    private Mono<Notification> notificationAvailableToInteraction(String notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
                .switchIfEmpty(Mono.error(new NotificationNotExistException(NotificationServiceConstants.EX_NOTIFIC_NOT_EXIST)))
                .filter(notification -> notification.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new NotificationNotAvailableException(NotificationServiceConstants.EX_NOTIFIC_NOT_AVAILABLE)));
    }
}













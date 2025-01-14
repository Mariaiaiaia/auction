package com.maria.service;

import com.maria.config.ReactiveKafkaConfig;
import com.maria.core.entity.AuctionFinishedNotificationEvent;
import com.maria.core.entity.AuctionItemEvent;
import com.maria.core.entity.NewBidNotificationEvent;
import com.maria.core.entity.UserRemovedNotificationEvent;
import com.maria.entity.Notification;
import com.maria.exception.*;
import com.maria.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;
    private final ReactiveKafkaConfig kafkaConfig;
    private final WebClient webClient;

    @Override
    public Flux<Notification> getNotificationForUser (Long userId){
        return notificationRepository.findByUserId(userId)
                .switchIfEmpty(Flux.empty());
    }

    @PostConstruct
    public void initialize() {
        ReactiveKafkaConsumerTemplate<String, NewBidNotificationEvent> newBitNotificationConsumer = kafkaConfig
                .createReactiveKafkaConsumerTemplate("new-bid-notification-events", NewBidNotificationEvent.class, "notification-group");
        ReactiveKafkaConsumerTemplate<String, UserRemovedNotificationEvent> userRemovedNotificationConsumer = kafkaConfig
                .createReactiveKafkaConsumerTemplate("user-removed-notification-events", UserRemovedNotificationEvent.class, "notification-group");
        ReactiveKafkaConsumerTemplate<String, AuctionFinishedNotificationEvent> auctionFinishedNotificationConsumer = kafkaConfig
                .createReactiveKafkaConsumerTemplate("auction-finished-notification-events", AuctionFinishedNotificationEvent.class, "notification-group");
        ReactiveKafkaConsumerTemplate<String, AuctionItemEvent> auctionDeletedConsumerTemplate = kafkaConfig
                .createReactiveKafkaConsumerTemplate("delete-auction-events", AuctionItemEvent.class, "auction-consumer-group");

        listenToKafkaTopic(newBitNotificationConsumer, this::processNewBidNotificationsEvent);
        listenToKafkaTopic(userRemovedNotificationConsumer, this::processUserRemovedNotificationsEvent);
        listenToKafkaTopic(auctionFinishedNotificationConsumer, this::processAuctionFinishedNotificationsEvent);
        listenToKafkaTopic(auctionDeletedConsumerTemplate, this::processAuctionDeletedNotificationsEvent);
    }

    private <T> void listenToKafkaTopic(ReactiveKafkaConsumerTemplate<String, T> consumerTemplate, Function<T, Mono<Void>> eventProcessor){
        consumerTemplate
                .receiveAutoAck()
                .flatMap(record -> eventProcessor.apply(record.value()))
                .doOnError(error -> log.error("Error in Kafka notification consumer: {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }


    private Mono<Void> processAuctionDeletedNotificationsEvent(AuctionItemEvent event) {
        return removeNotificationsByAuction(event.getAuctionId())
                .then();
    }


    private Mono<Void> processAuctionFinishedNotificationsEvent(AuctionFinishedNotificationEvent event) {
        return getUserIdForAuctionNotification(event.getAuctionId())
                .flatMap(userId -> {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setMessage("The auction is finished: " + event.getAuctionId());
                    notification.setTimestamp(event.getTimestamp());
                    notification.setItemId(event.getItemId());
                    notification.setRead(false);

                    return notificationRepository.save(notification)
                            .doOnSuccess(saved -> log.info("Auction finished notification successfully saved"))
                            .onErrorResume(ex -> {
                                log.error("Failed to save auction finished notification: {}", ex.getMessage());
                                return Mono.error(new DatabaseOperationException("Failed to save auction finished notification"));
                            });
                })
                .then();
    }


    private Mono<Void> processNewBidNotificationsEvent(NewBidNotificationEvent event) {
        return getUserIdForAuctionNotification(event.getAuctionId())
                .flatMap(userId -> {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setMessage("The auction have a new bid: " + event.getNewBid());
                    notification.setTimestamp(event.getTimestamp());
                    notification.setItemId(event.getItemId());
                    notification.setRead(false);

                    return notificationRepository.save(notification)
                            .doOnSuccess(saved -> log.info("New bid notification successfully saved"))
                            .onErrorResume(ex -> {
                                log.error("Failed to save new bid notification: {}", ex.getMessage());
                                return Mono.error(new DatabaseOperationException("Failed to save new bid notification"));
                            });
                })
                .then();
    }


    private Mono<Void> processUserRemovedNotificationsEvent(UserRemovedNotificationEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setMessage("You removed from auction: " + event.getAuctionId());
        notification.setTimestamp(event.getTimestamp());
        notification.setItemId(event.getItemId());
        notification.setRead(false);

        return notificationRepository.save(notification)
                .doOnSuccess(saved -> log.info("User removed notification successfully saved"))
                .onErrorResume(ex -> {
                    log.error("Failed to save user removed notification: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to save user removed notification"));
                })
                .then();
    }


    private Flux<Long> getUserIdForAuctionNotification(Long auctionId){
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bids/bidders_id/{auctionId}")
                        .build(auctionId))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.error("Auction is not founded: {}", auctionId);
                    return Mono.error(new AuctionNotExistException("This auction does not exist"));
                })
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Failed to get the auction: {}", errorBody);
                                    return Mono.error(new NotificationWebClientException("Service error occurred"));
                                }))
                .bodyToFlux(Long.class);
    }


    private Flux<Void> removeNotificationsByAuction(Long auctionId){
        return notificationRepository.findByAuctionId(auctionId)
                .flatMap(notification -> deleteNotificationInService(notification.getId()));
    }


    private Mono<Void> deleteNotificationInService(String notificationId){
        return notificationRepository.findById(notificationId)
                .switchIfEmpty(Mono.error(new NotificationNotExistException("Notification does not exist")))
                .flatMap(notificationRepository::delete)
                .doOnSuccess(success -> log.info("Notification {} deleted successfully", notificationId))
                .onErrorResume(ex -> {
                    log.error("Failed to delete notification: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to delete notification"));
                });
    }

    @Override
    public Mono<Void> deleteNotificationForUser(String notificationId, Long userId){
        return notificationAvailableToInteraction(notificationId, userId)
                .flatMap(notificationRepository::delete)
                .doOnSuccess(success -> log.info("Notification {} deleted successfully", notificationId))
                .onErrorResume(ex -> {
                    log.error("Failed to delete notification: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to delete notification"));
                });
    }

    private Mono<Notification> notificationAvailableToInteraction(String notificationId, Long userId){
        return notificationRepository.findById(notificationId)
                .switchIfEmpty(Mono.error(new NotificationNotExistException("Notification does not exist")))
                .filter(notification -> !notification.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new NotificationNotAvailableException("Notification is not available")));
    }
}













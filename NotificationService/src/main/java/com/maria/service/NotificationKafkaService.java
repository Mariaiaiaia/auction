package com.maria.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;
import com.maria.constant.NotificationServiceConstants;
import com.maria.constant.NotificationServiceEventConstants;
import com.maria.core.entity.AuctionFinishedNotificationEvent;
import com.maria.core.entity.AuctionItemEvent;
import com.maria.core.entity.NewBidNotificationEvent;
import com.maria.entity.Notification;
import com.maria.exception.*;
import com.maria.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationKafkaService {
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServersConfig;
    @Value("${key-deserializer-class-config}")
    private String keyDeserializer;
    @Value("${value-deserializer-class-config}")
    private String valueDeserializer;
    @Value("${kafka-group-id.notification}")
    private String kafkaNotificationGroup;
    @Value("${kafka-group-id.auction}")
    private String kafkaAuctionGroup;

    private <T> ReactiveKafkaConsumerTemplate<String, T> createReactiveKafkaConsumerTemplate(String topic, Class<T> targetType, String groupId) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());

        ReceiverOptions<String, T> receiverOptions = ReceiverOptions.<String, T>create(consumerProps)
                .subscription(Collections.singleton(topic));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @PostConstruct
    public void initialize() {
        ReactiveKafkaConsumerTemplate<String, NewBidNotificationEvent> newBitNotificationConsumer =
                createReactiveKafkaConsumerTemplate(NotificationServiceEventConstants.NEW_BID, NewBidNotificationEvent.class, kafkaNotificationGroup);
        ReactiveKafkaConsumerTemplate<String, AuctionFinishedNotificationEvent> auctionFinishedNotificationConsumer =
                createReactiveKafkaConsumerTemplate(NotificationServiceEventConstants.AUCTION_FINISHED, AuctionFinishedNotificationEvent.class, kafkaNotificationGroup);
        ReactiveKafkaConsumerTemplate<String, AuctionItemEvent> auctionDeletedConsumerTemplate =
                createReactiveKafkaConsumerTemplate(NotificationServiceEventConstants.DELETE_AUCTION, AuctionItemEvent.class, kafkaAuctionGroup);

        listenToKafkaTopic(newBitNotificationConsumer, this::processNewBidNotificationsEvent);
        listenToKafkaTopic(auctionFinishedNotificationConsumer, this::processAuctionFinishedNotificationsEvent);
        listenToKafkaTopic(auctionDeletedConsumerTemplate, this::processAuctionDeletedNotificationsEvent);
    }

    private <T> void listenToKafkaTopic(ReactiveKafkaConsumerTemplate<String, T> consumerTemplate, Function<T, Mono<Void>> eventProcessor) {
        consumerTemplate
                .receiveAutoAck()
                .flatMap(record -> eventProcessor.apply(record.value()))
                .doOnError(error -> log.error(NotificationServiceConstants.LOG_ERROR_CONSUMER, error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    private Mono<Void> processAuctionDeletedNotificationsEvent(AuctionItemEvent event) {
        return notificationService.removeNotificationsByAuction(event.getAuctionId())
                .then();
    }

    private Mono<Void> processAuctionFinishedNotificationsEvent(AuctionFinishedNotificationEvent event) {
        return notificationService.getUserIdForAuctionNotification(event.getAuctionId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.info(NotificationServiceConstants.LOG_NO_USERS_TO_NOTIFICATION, event.getAuctionId());
                    return Mono.empty();
                }))
                .flatMap(userId -> {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setAuctionId(event.getAuctionId());
                    notification.setMessage(NotificationServiceConstants.MESSAGE_AUCTION_FINISHED + event.getAuctionId());
                    notification.setTimestamp(event.getTimestamp());
                    notification.setItemId(event.getItemId());
                    notification.setRead(false);

                    return notificationRepository.save(notification)
                            .doOnSuccess(saved -> log.info(NotificationServiceConstants.LOG_NOTIFIC_SAVED))
                            .onErrorResume(ex -> {
                                log.error(NotificationServiceConstants.LOG_NOT_SAVED, ex.getMessage());
                                return Mono.error(new DatabaseOperationException(NotificationServiceConstants.EX_AUCTION_FINISH));
                            });
                })
                .then();
    }

    private Mono<Void> processNewBidNotificationsEvent(NewBidNotificationEvent event) {
        return notificationService.getUserIdForAuctionNotification(event.getAuctionId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.info(NotificationServiceConstants.LOG_NO_USERS_TO_NOTIFICATION, event.getAuctionId());
                    return Mono.empty();
                }))
                .flatMap(userId -> {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setAuctionId(event.getAuctionId());
                    notification.setMessage(NotificationServiceConstants.MESSAGE_NEW_BID + event.getNewBid());
                    notification.setTimestamp(event.getTimestamp());
                    notification.setItemId(event.getItemId());
                    notification.setRead(false);

                    return notificationRepository.save(notification)
                            .doOnSuccess(saved -> log.info(NotificationServiceConstants.LOG_NOTIFIC_SAVED))
                            .onErrorResume(ex -> {
                                log.error(NotificationServiceConstants.LOG_NOT_SAVED, ex.getMessage());
                                return Mono.error(new DatabaseOperationException(NotificationServiceConstants.EX_NEW_BID));
                            });
                })
                .then();
    }
}

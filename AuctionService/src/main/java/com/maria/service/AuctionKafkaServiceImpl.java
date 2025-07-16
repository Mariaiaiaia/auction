package com.maria.service;

import com.maria.constant.AuctionServiceConstants;
import com.maria.constant.AuctionServiceEventConstants;
import com.maria.core.entity.*;
import com.maria.entity.Auction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuctionKafkaServiceImpl implements AuctionKafkaService {
    private ReactiveKafkaConsumerTemplate<String, NewBitEvent> bitConsumerTemplate;
    private ReactiveKafkaConsumerTemplate<String, AcceptanceEvent> acceptanceConsumerTemplate;
    private ReactiveKafkaProducerTemplate<String, InvitationEvent> invitationProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, AuctionItemEvent> removeAuctionProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, Notification> notificationProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, AuctionItemEvent> createAuctionProducerTemplate;
    @Value("${kafka-group-id.bid}")
    private String kafkaBidGroup;
    @Value("${kafka-group-id.acceptance}")
    private String kafkaAcceptanceGroup;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServersConfig;

    public <T> ReactiveKafkaProducerTemplate<String, T> createReactiveKafkaProducerTemplate() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(producerProps));
    }

    public <T> ReactiveKafkaConsumerTemplate<String, T> createReactiveKafkaConsumerTemplate(String topic, Class<T> targetType, String groupId) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonDeserializer");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());

        ReceiverOptions<String, T> receiverOptions = ReceiverOptions.<String, T>create(consumerProps)
                .subscription(Collections.singleton(topic));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @PostConstruct
    private void initialize() {
        this.bitConsumerTemplate = createReactiveKafkaConsumerTemplate(AuctionServiceEventConstants.NEW_BID, NewBitEvent.class, kafkaBidGroup);
        this.acceptanceConsumerTemplate = createReactiveKafkaConsumerTemplate(AuctionServiceEventConstants.ACCEPTANCE, AcceptanceEvent.class, kafkaAcceptanceGroup);
        this.invitationProducerTemplate = createReactiveKafkaProducerTemplate();
        this.removeAuctionProducerTemplate = createReactiveKafkaProducerTemplate();
        this.notificationProducerTemplate = createReactiveKafkaProducerTemplate();
        this.createAuctionProducerTemplate = createReactiveKafkaProducerTemplate();
    }

    @Override
    public void listenToBids(Function<NewBitEvent, Mono<Void>> eventHandler) {
        bitConsumerTemplate
                .receiveAutoAck()
                .concatMap(record -> {
                    NewBitEvent event = record.value();
                    log.info(AuctionServiceConstants.LOG_BID_EVENT_NEW_BID, event.getBidAmount(), event.getBidderId());
                    return eventHandler.apply(event);
                })
                .doOnError(error -> log.error(AuctionServiceConstants.LOG_ERROR_KAFKA_BID_CONSUMER, error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    @Override
    public void listenToAcceptances(Function<AcceptanceEvent, Mono<Void>> eventHandler) {
        acceptanceConsumerTemplate
                .receiveAutoAck()
                .flatMap(record -> {
                    AcceptanceEvent event = record.value();
                    log.info(AuctionServiceConstants.LOG_ACCEPTANCE_EVENT, event.getUserId());
                    return eventHandler.apply(event);
                })
                .doOnError(error -> log.error(AuctionServiceConstants.LOG_ERROR_KAFKA_ACCEPTANCE_CONSUMER + error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    private Mono<Void> sendNotification(String topic, Notification notification) {
        return notificationProducerTemplate.send(topic, notification)
                .doOnSuccess(result -> log.info(AuctionServiceConstants.LOG_NOTIFICATION_SENT_TO_TOPIC, topic))
                .doOnError(ex -> log.error(AuctionServiceConstants.LOG_FAIL_SEND_NOTIFICATION_FOR_TOPIC, topic, ex.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }

    @Override
    public Mono<Void> sendNewBidNotificationEvent(Auction auction) {
        NewBidNotificationEvent notificationEvent = NewBidNotificationEvent
                .builder()
                .auctionId(auction.getAuctionId())
                .newBid(auction.getCurrentPrice())
                .timestamp(LocalDateTime.now())
                .type(NotificationType.NEW_BID)
                .bidderId(auction.getBidderId())
                .build();

        return sendNotification(AuctionServiceEventConstants.NEW_BID_NOTIFICATION, notificationEvent);
    }

    @Override
    public Mono<Void> sendAuctionFinishedNotificationEvent(Auction auction) {
        AuctionFinishedNotificationEvent notificationEvent = AuctionFinishedNotificationEvent
                .builder()
                .auctionId(auction.getAuctionId())
                .itemId(auction.getItemId())
                .timestamp(LocalDateTime.now())
                .finalPrice(auction.getCurrentPrice())
                .build();

        return sendNotification(AuctionServiceEventConstants.AUCTION_FINISHED_NOTIFICATION, notificationEvent);
    }

    @Override
    public Mono<Void> sendInvitationEvent(Long auctionId, Long sellerId, Long userId) {
        return invitationProducerTemplate.send(AuctionServiceEventConstants.AUCTION_INVITATION, new InvitationEvent(auctionId, sellerId, userId))
                .doOnSuccess(result -> log.info(AuctionServiceConstants.LOG_INVITATION_EVENT_SENT, auctionId, userId))
                .doOnError(error -> log.error(AuctionServiceConstants.LOG_FAIL_SEND_INVITATION, auctionId, userId, error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }

    @Override
    public Mono<Void> sendAuctionCreatedEvent(Auction auction) {
        return createAuctionProducerTemplate.send(AuctionServiceEventConstants.AUCTION_CREATED, new AuctionItemEvent(auction.getAuctionId(), auction.getItemId()))
                .doOnSuccess(result -> log.info(AuctionServiceConstants.LOG_AUCTION_CREATED_EVENT_SENT, auction.getAuctionId()))
                .doOnError(error -> log.error(AuctionServiceConstants.LOG_FAIL_SEND_AUCTION_CREATED_EVENT, auction.getAuctionId()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }

    @Override
    public Mono<Void> sendAuctionRemovedEvent(Auction auction) {
        return removeAuctionProducerTemplate.send(AuctionServiceEventConstants.DELETE_AUCTION, new AuctionItemEvent(auction.getAuctionId(), auction.getItemId()))
                .doOnSuccess(result -> log.info(AuctionServiceConstants.LOG_AUCTION_REMOVED_EVENT_SENT, auction.getAuctionId()))
                .doOnError(error -> log.error(AuctionServiceConstants.LOG_FAIL_SEND_AUCTION_REMOVED_EVENT, auction.getAuctionId()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }
}

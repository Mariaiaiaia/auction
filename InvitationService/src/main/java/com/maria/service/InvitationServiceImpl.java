package com.maria.service;

import com.maria.constant.InvitationServiceConstants;
import com.maria.constant.InvitationServiceEventConstants;
import com.maria.core.entity.AcceptanceEvent;
import com.maria.core.entity.InvitationEvent;
import com.maria.entity.Invitation;
import com.maria.exception.DatabaseOperationException;
import com.maria.exception.InvitationNotExistException;
import com.maria.repository.InvitationRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {
    private final InvitationRepository invitationRepository;
    private ReactiveKafkaConsumerTemplate<String, InvitationEvent> invitationConsumerTemplate;
    private ReactiveKafkaProducerTemplate<String, AcceptanceEvent> acceptanceProducerTemplate;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServersConfig;
    @Value("${kafka-group-id.invitation}")
    private String kafkaInvitationGroup;

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
    public void initialize() {
        String invitationTopic = InvitationServiceEventConstants.AUCTION_INVITATION;
        this.invitationConsumerTemplate = createReactiveKafkaConsumerTemplate(invitationTopic, InvitationEvent.class, kafkaInvitationGroup);
        this.acceptanceProducerTemplate = createReactiveKafkaProducerTemplate();

        listenToInvitation();
    }

    @Override
    public Flux<Invitation> getInvitationsForUser(Long userId) {
        return invitationRepository.findByUserId(userId)
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn(InvitationServiceConstants.LOG_NO_INVITATIONS, userId);
                    return Flux.empty();
                }))
                .onErrorResume(ex -> {
                    log.error(InvitationServiceConstants.LOG_ERROR_RETRIEVING_INVITATIONS, ex.getMessage());
                    return Mono.error(new DatabaseOperationException(InvitationServiceConstants.EX_FAIL_GET_INVITATIONS));
                });
    }

    private void listenToInvitation() {
        invitationConsumerTemplate
                .receiveAutoAck()
                .flatMap(record -> {
                    InvitationEvent event = record.value();
                    log.info(InvitationServiceConstants.LOG_NEW_INVITATION, event.getAuctionId(), event.getUserId());
                    return processInvitation(event);
                })
                .doOnError(error -> log.error(InvitationServiceConstants.LOG_ERROR_CONSUMER, error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    private Mono<Void> processInvitation(InvitationEvent event) {
        Invitation newInvitation = Invitation.builder()
                .auctionId(event.getAuctionId())
                .sellerId(event.getSellerId())
                .userId(event.getUserId())
                .acceptance(null)
                .build();

        return invitationRepository.save(newInvitation)
                .doOnSuccess(savedInvitation ->
                        log.info(InvitationServiceConstants.LOG_INVITATION_SAVED, savedInvitation.getInvitationId()))
                .onErrorResume(ex -> {
                    log.error(InvitationServiceConstants.LOG_FAIL_SAVE_INVITATION, ex.getMessage());
                    return Mono.error(new DatabaseOperationException(InvitationServiceConstants.EX_FAIL_SAVE_INVITATION));
                })
                .then();
    }

    @Override
    public Mono<Void> respondToInvitation(Long userId, Long auctionId, boolean accepted) {
        AcceptanceEvent responseEvent = AcceptanceEvent.builder()
                .auctionId(auctionId)
                .userId(userId)
                .acceptance(accepted)
                .build();

        return updateAcceptance(userId, auctionId, accepted)
                .then(acceptanceProducerTemplate.send(InvitationServiceEventConstants.ACCEPTANCE, responseEvent))
                .doOnSuccess(result -> log.info(InvitationServiceConstants.LOG_ACCEPTANCE_SENT))
                .doOnError(error -> log.error(InvitationServiceConstants.LOG_ERROR_PRODUCER, error.getMessage()))
                .then();
    }

    private Mono<Void> updateAcceptance(Long userId, Long auctionId, boolean acceptance) {
        return invitationRepository.findByUserIdAndAuctionId(userId, auctionId)
                .switchIfEmpty(Mono.error(new InvitationNotExistException(InvitationServiceConstants.EX_INVITATION_NOT_FOUND)))
                .flatMap(invitation -> {
                    invitation.setAcceptance(acceptance);
                    return invitationRepository.save(invitation);
                })
                .doOnSuccess(success -> log.info(InvitationServiceConstants.LOG_INVITATION_UPDATED))
                .onErrorMap(ex -> {
                    log.error(InvitationServiceConstants.LOG_FAIL_UPDATE_INVITATION, ex.getMessage());
                    if (ex instanceof InvitationNotExistException) {
                        return ex;
                    }
                    return new DatabaseOperationException(InvitationServiceConstants.EX_FAIL_UPDATE_INVITATION);
                })
                .then();
    }
}

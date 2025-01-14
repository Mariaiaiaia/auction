package com.maria.service;

import com.maria.config.ReactiveKafkaConfig;
import com.maria.core.entity.AcceptanceEvent;
import com.maria.core.entity.InvitationEvent;
import com.maria.core.entity.NewBitEvent;
import com.maria.entity.Invitation;
import com.maria.exception.DatabaseOperationException;
import com.maria.repository.InvitationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService{
    private final InvitationRepository invitationRepository;
    private ReactiveKafkaConsumerTemplate<String, InvitationEvent> invitationConsumerTemplate;
    private ReactiveKafkaProducerTemplate<String, AcceptanceEvent> acceptanceProducerTemplate;
    private final ReactiveKafkaConfig kafkaConfig;

    @PostConstruct
    public void initialize() {
        String invitationTopic = "auction-invitations-events";
        this.invitationConsumerTemplate = kafkaConfig.createReactiveKafkaConsumerTemplate(invitationTopic, InvitationEvent.class, "invitation-consumer-group");
        this.acceptanceProducerTemplate = kafkaConfig.createReactiveKafkaProducerTemplate();

        listenToInvitation();
    }

    @Override
    public Flux<Invitation> getInvitationsForUser(Long userId){
        return invitationRepository.findByUserId(userId)
                .onErrorResume(ex -> {
                    log.error("Error occurred while retrieving user invitations: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to get invitations"));
                });
    }


    private void listenToInvitation(){
        invitationConsumerTemplate
                .receiveAutoAck()
                .flatMap(record -> {
                    InvitationEvent event = record.value();
                    log.info("Invitation event: auction: {} user: {}",event.getAuctionId(), event.getUserId());

                    return processInvitation(event);
                })
                .doOnError(error -> log.error("Error in Kafka bid consumer: {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }


    private Mono<Void> processInvitation(InvitationEvent event){
        Invitation newInvitation = Invitation.builder()
                .auctionId(event.getAuctionId())
                .sellerId(event.getSellerId())
                .userId(event.getUserId())
                .acceptance(null)
                .build();

        return invitationRepository.save(newInvitation)
                .doOnSuccess(savedInvitation -> log.info("Invitation successfully saved, invitation id: {}", savedInvitation.getInvitationId()))
                .onErrorResume(ex -> {
                    log.error("Failed to save auction: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to save invitation"));
                })
                .then();
    }

    @Override
    public Mono<Void> respondToInvitation(Long userId, Long auctionId, boolean accepted){
        AcceptanceEvent responseEvent = AcceptanceEvent.builder()
                .auctionId(auctionId)
                .userId(userId)
                .acceptance(accepted)
                .build();

        return updateAcceptance(userId, auctionId, accepted)
                .then(acceptanceProducerTemplate.send("acceptance-events", responseEvent))
                .doOnSuccess(result -> log.info("Acceptance sent successfully"))
                .doOnError(error -> log.error("Error in Kafka acceptance producer: {}", error.getMessage()))
                .then();
    }


    private Mono<Void> updateAcceptance(Long userId, Long auctionId, boolean acceptance){
        return invitationRepository.findByUserIdAndAuctionId(userId, auctionId)
                .flatMap(invitation -> {
                    invitation.setAcceptance(acceptance);

                    return invitationRepository.save(invitation)
                            .doOnSuccess(success -> log.info(("Invitation successfully updated")))
                            .onErrorResume(ex -> {
                                log.error("Failed to update acceptance: {}", ex.getMessage());
                                return Mono.error(new DatabaseOperationException("Failed to update acceptance"));
                            });
                })
                .then();
    }
}

package com.maria.service;

import com.maria.constant.BidServiceConstants;
import com.maria.constant.BidServiceEventConstants;
import com.maria.core.entity.*;
import com.maria.dto.PlaceBidRequest;
import com.maria.entity.Bid;
import com.maria.exception.*;
import com.maria.repository.BidRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private ReactiveKafkaProducerTemplate<String, NewBitEvent> producerTemplate;
    private final WebClient webClient;
    @Value("${bootstrap-servers-config}")
    private String bootstrapServersConfig;
    @Value("${kafka-group-id.auction}")
    private String kafkaAuctionGroup;
    @Value("${uri.get-seller}")
    private String getSellerUri;

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
        this.producerTemplate = createReactiveKafkaProducerTemplate();
        ReactiveKafkaConsumerTemplate<String, AuctionItemEvent> auctionDeletedConsumerTemplate =
                createReactiveKafkaConsumerTemplate(BidServiceEventConstants.DELETE_AUCTION, AuctionItemEvent.class, kafkaAuctionGroup);

        listenToKafkaTopic(auctionDeletedConsumerTemplate, this::processAuctionDeletedEvent);
    }

    private <T> void listenToKafkaTopic(ReactiveKafkaConsumerTemplate<String, T> consumerTemplate, Function<T, Mono<Void>> eventProcessor) {
        consumerTemplate
                .receiveAutoAck()
                .flatMap(record -> eventProcessor.apply(record.value()))
                .doOnError(error -> log.error(BidServiceConstants.LOG_ERROR_KAFKA_CONSUMER, error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    private Mono<Void> processAuctionDeletedEvent(AuctionItemEvent event) {
        return deleteBid(event.getAuctionId())
                .then();
    }

    @Override
    public Mono<Void> placeBid(PlaceBidRequest bidRequest, Long currentUserId) {
        return createBid(bidRequest, currentUserId)
                .flatMap(newBid -> {
                    NewBitEvent bitEvent = NewBitEvent.builder()
                            .bidAmount(newBid.getBidAmount())
                            .auctionId(newBid.getAuctionId())
                            .bidderId(newBid.getUserId())
                            .bidId(newBid.getBidId())
                            .build();
                    return producerTemplate.send(BidServiceEventConstants.NEW_BID, newBid.getAuctionId().toString(), bitEvent)
                            .then();
                })
                .doOnSuccess((success -> log.info(BidServiceConstants.LOG_BID_SAVED, bidRequest.getAuctionId(), bidRequest.getBidAmount())))
                .onErrorMap(ex -> {
                    log.warn(BidServiceConstants.LOG_FAIL_TO_SAVE_BID, ex.getMessage());
                    if (ex instanceof DatabaseOperationException || ex instanceof BidExistsException) {
                        return ex;
                    }
                    return new Exception(BidServiceConstants.EX_FAIL_TO_CREATE_BID);
                });

    }

    private Mono<Bid> createBid(PlaceBidRequest bidRequest, Long currentUserId) {
        Bid newBid = Bid.builder()
                .bidAmount(bidRequest.getBidAmount())
                .auctionId(bidRequest.getAuctionId())
                .userId(currentUserId)
                .build();

        return bidRepository.findByUserIdAndAuctionIdAndBidAmount(currentUserId, bidRequest.getAuctionId(), bidRequest.getBidAmount())
                .flatMap(existingBid ->
                        Mono.<Bid>error(new BidExistsException(BidServiceConstants.EX_BID_EXISTS)))
                .switchIfEmpty(Mono.defer(() -> bidRepository.save(newBid)
                        .doOnSuccess(savedBid -> log.info(BidServiceConstants.LOG_BID_SAVED, savedBid.getAuctionId(), savedBid.getBidAmount()))
                        .onErrorResume(ex -> {
                            log.warn(BidServiceConstants.LOG_FAIL_TO_SAVE_BID, ex.getMessage());
                            return Mono.error(new DatabaseOperationException(BidServiceConstants.EX_FAIL_TO_SAVE_BID));
                        })))
                .onErrorMap(ex -> {
                    log.warn(BidServiceConstants.LOG_FAIL_TO_SAVE_BID, ex.getMessage());
                    if (ex instanceof DatabaseOperationException || ex instanceof BidExistsException) {
                        return ex;
                    }
                    return new Exception(BidServiceConstants.EX_FAIL_TO_SAVE_BID);
                });
    }

    @Override
    public Flux<Bid> getAllUserBids(Long userId) {
        return bidRepository.findByUserId(userId)
                .onErrorResume(ex -> {
                    log.error(BidServiceConstants.LOG_ERROR_WHILE_RETRIEVING_BIDS, ex.getMessage());
                    return Mono.error(new DatabaseOperationException(BidServiceConstants.EX_FAIL_TO_GET_BIDS));
                });
    }

    @Override
    public Flux<Bid> getAllAuctionBids(Long auctionId, Long currentUserId) {
        return getAuctionWebClient(auctionId)
                .flatMapMany(sellerId -> {
                    if (!sellerId.equals(currentUserId)) {
                        return Flux.error(new AuctionNotAvailableException(BidServiceConstants.EX_BIDS_AVAILABLE));
                    }
                    return bidRepository.findByAuctionId(auctionId);
                })
                .onErrorMap(ex -> {
                    log.error(BidServiceConstants.LOG_ERROR_WHILE_RETRIEVING_BIDS, ex.getMessage());
                    if(ex instanceof AuctionNotAvailableException || ex instanceof BidWebClientException){
                        return ex;
                    }
                    return new Exception(BidServiceConstants.EX_FAIL_TO_GET_BIDS);
                });
    }

    @Override
    public Flux<Long> getAllAuctionBiddersId(Long auctionId) {
        return bidRepository.findByAuctionId(auctionId)
                .map(Bid::getUserId)
                .distinct()
                .onErrorResume(ex -> {
                    log.error(BidServiceConstants.LOG_ERROR_WHILE_RETRIEVING_BIDS, ex.getMessage());
                    return Flux.empty();
                });
    }

    private Mono<Long> getAuctionWebClient(Long auctionId) {
        return webClient
                .get()
                .uri(getSellerUri, auctionId)
                .header("X-Internal-Service", "true")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(BidServiceConstants.LOG_FAIL_TO_GET_AUCTION, errorBody);
                                    return Mono.error(new BidWebClientException(BidServiceConstants.EX_SERVICE_ERROR));
                                }))
                .bodyToMono(Long.class)
                .switchIfEmpty(Mono.error(new AuctionNotAvailableException(BidServiceConstants.EX_AUCTION_NOT_AVAILABLE)));
    }

    private Flux<Void> deleteBid(Long auctionId) {
        return bidRepository.findByAuctionId(auctionId)
                .flatMap(bidRepository::delete)
                .onErrorResume(ex -> {
                    log.error(BidServiceConstants.LOG_FAIL_TO_DELETE_BID, ex.getMessage());
                    return Mono.error(new DatabaseOperationException(BidServiceConstants.EX_FAIL_TO_DELETE_BID));
                });
    }
}

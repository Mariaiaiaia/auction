package com.maria.service;

import com.maria.config.ReactiveKafkaConfig;
import com.maria.core.entity.AuctionItemEvent;
import com.maria.dto.ItemDTO;
import com.maria.entity.Item;
import com.maria.entity.CreateItemRequest;
import com.maria.exception.DatabaseOperationException;
import com.maria.exception.ItemNotExistException;
import com.maria.mapper.ItemMapper;
import com.maria.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final S3Service s3Service;
    private final ReactiveKafkaConfig kafkaConfig;
    private ReactiveKafkaConsumerTemplate<String, AuctionItemEvent> auctionDeletedConsumerTemplate;

    @PostConstruct
    public void initialize() {
        this.auctionDeletedConsumerTemplate = kafkaConfig.createReactiveKafkaConsumerTemplate("delete-auction-events", AuctionItemEvent.class, "auction-consumer-group");
        listenToRemoves();
    }

    private void listenToRemoves(){
        auctionDeletedConsumerTemplate
                .receiveAutoAck()
                .flatMap(record -> {
                    log.info("Auction {} was deleted, process to remove item {}",
                            record.value().getAuctionId(), record.value().getItemId());
                    return deleteItem(record.value().getItemId());})
                .doOnError(error -> log.error("Error in Kafka item consumer: {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    @Override
    public Mono<ItemDTO> getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .map(itemMapper::toDto)
                .switchIfEmpty(Mono.error(new ItemNotExistException("Item does not exist")))
                .onErrorResume(ex -> {
                    log.error("Error occurred while retrieving item: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to get item"));
                });
    }

    @Override
    public Mono<ItemDTO> createItem(CreateItemRequest itemRequest, Long sellerId, byte[] file){
        String key = UUID.randomUUID().toString();

        return s3Service.uploadDataToS3("bucketName", key, file)
                .flatMap(imgUrl -> itemRepository.save(Item.builder()
                        .itemName(itemRequest.getItemName())
                        .description(itemRequest.getDescription())
                        .image(imgUrl)
                        .sellerId(sellerId)
                        .isSold(false)
                        .build()))
                .map(itemMapper::toDto)
                .onErrorResume(ex -> {
                    log.error("Error while creating item: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to create item"));
                });
    }

    @Override
    public Mono<Void> deleteItem(Long itemId){
        return itemRepository.findById(itemId)
                .flatMap(itemRepository::delete)
                .switchIfEmpty(Mono.error(new ItemNotExistException("This item does not exist")))
                .onErrorResume(ex -> {
                    log.error("Failed to delete item {}: {}", itemId, ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to delete item"));
                });
    }
}

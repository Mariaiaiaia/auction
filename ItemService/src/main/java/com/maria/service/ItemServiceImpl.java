package com.maria.service;

import com.maria.constant.ItemServiceConstants;
import com.maria.dto.ItemDTO;
import com.maria.entity.Item;
import com.maria.entity.CreateItemRequest;
import com.maria.exception.DatabaseOperationException;
import com.maria.exception.ItemNotExistException;
import com.maria.mapper.ItemMapper;
import com.maria.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final S3Service s3Service;
    @Value("${s3.bucket.name}")
    private String s3Bucket;

    @Override
    public Mono<ItemDTO> getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .map(itemMapper::toDto)
                .switchIfEmpty(Mono.error(new ItemNotExistException(ItemServiceConstants.EX_ITEM_NOT_EXIST)))
                .onErrorMap(ex -> {
                    log.error(ItemServiceConstants.LOG_ERROR_RETRIEVING_ITEM, ex.getMessage());
                    if (ex instanceof ItemNotExistException) {
                        return ex;
                    }
                    return new DatabaseOperationException(ItemServiceConstants.EX_FAIL_GET_ITEM);
                });
    }

    @Override
    public Mono<ItemDTO> createItem(CreateItemRequest itemRequest, Long sellerId, byte[] file) {
        String key = UUID.randomUUID().toString();

        return s3Service.uploadDataToS3(s3Bucket, key, file)
                .flatMap(imgUrl -> itemRepository.save(Item.builder()
                        .itemName(itemRequest.getItemName())
                        .description(itemRequest.getDescription())
                        .image(imgUrl)
                        .sellerId(sellerId)
                        .isSold(false)
                        .build()))
                .map(itemMapper::toDto)
                .onErrorResume(ex -> {
                    log.error(ItemServiceConstants.LOG_ERROR_CREATING_ITEM, ex.getMessage());
                    return Mono.error(new DatabaseOperationException(ItemServiceConstants.EX_FAIL_CREATE_ITEM));
                });
    }

    @Override
    public Mono<Long> getSellerId(Long itemId) {
        return itemRepository.findById(itemId)
                .map(Item::getSellerId)
                .switchIfEmpty(Mono.error(new ItemNotExistException(ItemServiceConstants.EX_ITEM_NOT_EXIST)))
                .onErrorMap(ex -> {
                    log.error(ItemServiceConstants.LOG_ERROR_RETRIEVING_ITEM, ex.getMessage());
                    if (ex instanceof ItemNotExistException) {
                        return ex;
                    }
                    return new DatabaseOperationException(ItemServiceConstants.EX_FAIL_GET_ITEM);
                });
    }
}

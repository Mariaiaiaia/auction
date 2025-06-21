package com.maria.service;

import com.maria.dto.ItemDTO;
import com.maria.entity.CreateItemRequest;
import reactor.core.publisher.Mono;

public interface ItemService {
    Mono<ItemDTO> getItem(Long itemId);

    Mono<ItemDTO> createItem(CreateItemRequest itemRequest, Long sellerId, byte[] file);

    Mono<Long> getSellerId(Long itemId);
}

package com.maria.repository;

import com.maria.entity.Item;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemRepository extends R2dbcRepository<Item, Long> {
    Flux<Item> findBySellerId(Long sellerId);

    Mono<Item> findBySellerIdAndItemName(Long sellerId, String itemName);
}

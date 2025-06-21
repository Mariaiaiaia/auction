package com.maria.repository;

import com.maria.entity.Auction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface AuctionRepository extends R2dbcRepository<Auction, Long> {
    Flux<Auction> findBySellerId(Long userId);

    Flux<Auction> findByFinishedIsFalseAndPublicAccessIsTrue();

    Flux<Auction> findByFinishedIsFalse();

    Mono<Auction> findByItemId(Long itemId);

    Flux<Auction> findByEndDateBetween(LocalDateTime fromDate, LocalDateTime toDate);
}

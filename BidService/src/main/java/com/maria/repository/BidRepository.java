package com.maria.repository;

import com.maria.entity.Bid;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BidRepository extends R2dbcRepository<Bid, Long> {
    Flux<Bid> findByAuctionId(Long auctionId);

    Flux<Bid> findByUserId(Long userId);

    Mono<Bid> findByUserIdAndAuctionIdAndBidAmount(Long userId, Long auctionId, BigDecimal bidAmount);
}

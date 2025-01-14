package com.maria.repository;

import com.maria.entity.Bid;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface BidRepository extends R2dbcRepository<Bid, Long> {
    Flux<Bid> findByAuctionId(Long auctionId);
    Flux<Bid> findByUserId(Long userId);
}

package com.maria.service;

import com.maria.dto.PlaceBidRequest;
import com.maria.entity.Bid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BidService {
    Mono<Void> placeBid(PlaceBidRequest bidRequest, Long currentUserId);

    Flux<Bid> getAllAuctionBids(Long auctionId, Long currentUserId);

    Flux<Bid> getAllUserBids(Long userId);

    Flux<Long> getAllAuctionBiddersId(Long auctionId);
}

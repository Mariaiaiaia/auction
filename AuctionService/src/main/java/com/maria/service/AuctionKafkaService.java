package com.maria.service;

import com.maria.core.entity.AcceptanceEvent;
import com.maria.core.entity.NewBitEvent;
import com.maria.entity.Auction;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface AuctionKafkaService {
    Mono<Void> sendNewBidNotificationEvent(Auction auction);

    Mono<Void> sendAuctionFinishedNotificationEvent(Auction auction);

    Mono<Void> sendInvitationEvent(Long auctionId, Long sellerId, Long userId);

    Mono<Void> sendAuctionCreatedEvent(Auction auction);

    Mono<Void> sendAuctionRemovedEvent(Auction auction);

    void listenToAcceptances(Function<AcceptanceEvent, Mono<Void>> eventHandler);

    void listenToBids(Function<NewBitEvent, Mono<Void>> eventHandler);
}

package com.maria.service;

import com.maria.core.entity.AcceptanceEvent;
import com.maria.core.entity.AuctionDTO;
import com.maria.core.entity.NewBitEvent;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AuctionService {
    Mono<AuctionDTO> createAuction(CreateAuctionRequestDTO auctionRequest, Long currentUserId);

    Mono<AuctionDTO> getAuction(Long auctionId, Long currentUserId);

    Flux<AuctionDTO> getAllActivePublicAuctions();

    Mono<AuctionDTO> closeAuction(Long auctionId, Long currentUserId);

    Mono<Void> deleteAuction(Long auctionId, Long currentUserId);

    Mono<AuctionDTO> updateAuction(Long currentUserId, Long auctionId, AuctionUpdateDTO auctionUpdateDTO);

    Flux<AuctionDTO> getAllActiveAuctionsForUser(Long userId);

    Flux<AuctionDTO> getAllSellersAuctions(Long userId);

    Mono<Void> sendInvitation(Long auctionId, Long sellerId, List<String> userEmails);

    Flux<AuctionDTO> getAllActivePrivateAuctions(Long userId);

    Mono<Void> updateHighestBid(NewBitEvent newBitEvent);

    Mono<Void> processAcceptanceEvent(AcceptanceEvent acceptanceEvent);

    Mono<Long> getSellerId(Long auctionId);
}

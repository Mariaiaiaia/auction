package com.maria.service;

import com.maria.config.ReactiveKafkaConfig;
import com.maria.core.entity.AuctionDTO;
import com.maria.core.entity.NewBitEvent;
import com.maria.dto.PlaceBidRequest;
import com.maria.entity.Bid;
import com.maria.exception.*;
import com.maria.repository.BidRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private ReactiveKafkaProducerTemplate<String, NewBitEvent> producerTemplate;
    private final ReactiveKafkaConfig kafkaConfig;
    private final WebClient webClient;

    @PostConstruct
    public void initialize(){
        this.producerTemplate = kafkaConfig.createReactiveKafkaProducerTemplate();
    }

    @Override
    public Mono<Void> placeBid(PlaceBidRequest bidRequest, Long currentUserId) {
        if(bidRequest.getBidAmount().compareTo(BigDecimal.ZERO) <= 0){
            return Mono.error(new DataForBidNotValidException("The bid must be greater than zero"));
        }else {
            return createBid(bidRequest, currentUserId)
                    .flatMap(newBid -> {
                        NewBitEvent bitEvent = NewBitEvent.builder()
                                .bidAmount(newBid.getBidAmount())
                                .auctionId(newBid.getAuctionId())
                                .bidderId(newBid.getUserId())
                                .bidId(newBid.getBidId())
                                .build();
                        return producerTemplate.send("new-bid-events", newBid.getAuctionId().toString(), bitEvent)
                                .then();
                    })
                    .doOnSuccess((success -> log.info("Bid successfully saved, auction id: {}, bid amount: {}", bidRequest.getAuctionId(), bidRequest.getBidAmount())))
                    .onErrorMap(ex -> {
                        if(ex instanceof DatabaseOperationException){
                            return ex;
                        }
                        log.warn("Unexpected error: {}", ex.getMessage());
                        return new Exception("Failed to create bid");
                    });
        }
    }


    private Mono<Bid> createBid(PlaceBidRequest bidRequest, Long currentUserId) {
        Bid newBid = Bid.builder()
                .bidAmount(bidRequest.getBidAmount())
                .auctionId(bidRequest.getAuctionId())
                .userId(currentUserId)
                .build();
        return bidRepository.save(newBid)
                .doOnSuccess(savedBid -> log.info("Bid successfully saved, auction id: {}", savedBid.getBidId()))
                .onErrorResume(ex -> {
                    log.warn("Failed to save bid: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to save bid"));
                });
    }

    @Override
    public Flux<Bid> getAllUserBids(Long userId){
        return bidRepository.findByUserId(userId)
                .onErrorResume(ex -> {
                    log.error("Error occurred while retrieving user bids: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to get bids"));
                });
    }

    @Override
    public Flux<Bid> getAllAuctionBids(Long auctionId, Long currentUserId){
        return getAuctionWebClient(auctionId)
                .filter(auction -> auction.getSellerId().equals(currentUserId))
                .flatMapMany(auctionValidate -> bidRepository.findByAuctionId(auctionValidate.getAuctionId()))
                .onErrorResume(ex -> {
                    log.error("Error occurred while retrieving auction bids: {}", ex.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Flux<Long> getAllAuctionBiddersId(Long auctionId){
        return bidRepository.findByAuctionId(auctionId)
                .map(Bid::getUserId)
                .distinct()
                .onErrorResume(ex -> {
                    log.error("Error retrieving bidders: {}", ex.getMessage());
                    return Flux.empty();
                });
    }

    private Mono<AuctionDTO> getAuctionWebClient(Long auctionId) {
        return webClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.error("Auction is not founded: {}", auctionId);
                    return Mono.error(new AuctionNotExistException("This auction does not exist"));
                })
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Failed to get the item: {}", errorBody);
                                    return Mono.error(new BidWebClientException("Service error occurred"));
                                }))
                .bodyToMono(AuctionDTO.class);
    }
}

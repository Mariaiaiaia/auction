package com.maria.handler;

import com.maria.constant.BidServiceConstants;
import com.maria.dto.PlaceBidRequest;
import com.maria.service.BidService;
import com.maria.validator.BidServiceValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class BidHandler {
    private final BidService bidService;
    private final BidServiceValidation bidServiceValidation;

    public Mono<ServerResponse> placeBid(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(request.bodyToMono(PlaceBidRequest.class)
                        .flatMap(bidServiceValidation::validatePlaceBidRequest))
                .flatMap(tuple -> bidService.placeBid(tuple.getT2(), tuple.getT1())
                        .then(ServerResponse
                                .ok()
                                .bodyValue(BidServiceConstants.RESPONSE_BID_PLACED)));
    }

    public Mono<ServerResponse> getBiddersIdFromAuction(ServerRequest request) {
        return bidService.getAllAuctionBiddersId(Long.valueOf(request.pathVariable("auctionId")))
                .collectList()
                .flatMap(userIds ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(userIds));
    }

    public Mono<ServerResponse> getAllBidsForUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> bidService.getAllUserBids(userId)
                        .collectList()
                        .flatMap(userBids ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(userBids)));
    }

    public Mono<ServerResponse> getAllBidsForAuction(ServerRequest request) {
        return bidServiceValidation.validateId(request.pathVariable("auctionId"))
                .zipWith(ReactiveSecurityContextHolder.getContext()
                        .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString())))
                .flatMap(tuple -> bidService.getAllAuctionBids(tuple.getT1(), tuple.getT2())
                        .collectList()
                        .flatMap(userBids ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(userBids)));
    }
}

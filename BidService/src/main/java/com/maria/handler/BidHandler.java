package com.maria.handler;

import com.maria.dto.PlaceBidRequest;
import com.maria.service.BidService;
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

    public Mono<ServerResponse> placeBid(ServerRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(request.bodyToMono(PlaceBidRequest.class))
                .flatMap(tuple -> bidService.placeBid(tuple.getT2(), tuple.getT1())
                        .then(ServerResponse
                                .ok()
                                .bodyValue("bid sends")));
    }

    public Mono<ServerResponse> getBiddersIdFromAuction(ServerRequest request){
        return bidService.getAllAuctionBiddersId(Long.valueOf(request.pathVariable("auctionId")))
                .collectList()
                .flatMap(userIds ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(userIds));

    }

    public Mono<ServerResponse> getAllBidsForUser(ServerRequest request){
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

    public Mono<ServerResponse> getAllBidsForAuction(ServerRequest request){
        Long auctionId = Long.valueOf(request.pathVariable("auctionId"));
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> bidService.getAllAuctionBids(auctionId, userId)
                        .collectList()
                        .flatMap(userBids ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(userBids)));
    }
}

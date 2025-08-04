package com.maria.handler;

import com.maria.constant.AuctionServiceConstants;
import com.maria.core.entity.AuctionDTO;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import com.maria.service.AuctionService;
import com.maria.validator.AuctionServiceValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AuctionHandler {
    private final AuctionService auctionService;
    private final AuctionServiceValidation auctionServiceValidation;

    public Mono<ServerResponse> create(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> Long.valueOf(auth.getPrincipal().toString()))
                .zipWith(request.bodyToMono(CreateAuctionRequestDTO.class)
                        .flatMap(auctionServiceValidation::validateCreateAuctionRequestDTO))
                .flatMap(tuple -> auctionService.createAuction(tuple.getT2(), tuple.getT1()))
                .flatMap(auctionDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctionDTO));
    }

    public Mono<ServerResponse> sendInvitationToUser(ServerRequest request) {
        return request.bodyToMono(new ParameterizedTypeReference<List<String>>() {
                })
                .flatMap(invitedUserEmails ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .map(auth -> auth.getPrincipal().toString())
                                .flatMap(auctionServiceValidation::validateId)
                                .zipWith(auctionServiceValidation.validateId(request.pathVariable("auctionId")))
                                .flatMap(tuple -> auctionService.sendInvitation(tuple.getT2(), tuple.getT1(), invitedUserEmails))
                                .then(ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(AuctionServiceConstants.RESPONSE_INVITATION_SENT)));
    }

    public Mono<ServerResponse> getInfo(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(auctionServiceValidation.validateId(request.pathVariable("id")))
                .flatMap(tuple -> auctionService.getAuction(tuple.getT2(), tuple.getT1())
                        .flatMap(auctionDTO ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(auctionDTO)));
    }

    public Mono<ServerResponse> getSellerId(ServerRequest request) {
        return auctionService.getSellerId(Long.valueOf(request.pathVariable("id")))
                .flatMap(sellerId -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sellerId));
    }

    public Mono<ServerResponse> getActivePublicAuctions(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(auctionService.getAllActivePublicAuctions(), AuctionDTO.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(auctionServiceValidation.validateId(request.pathVariable("id")))
                .flatMap(tuple -> auctionService.deleteAuction(tuple.getT2(), tuple.getT1())
                        .then(ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(AuctionServiceConstants.AUCTION_DELETED)));
    }

    public Mono<ServerResponse> closeAuction(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(auctionServiceValidation.validateId(request.pathVariable("id")))
                .flatMap(tuple -> auctionService.closeAuction(tuple.getT2(), tuple.getT1())
                        .flatMap(auctionDTO ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(auctionDTO)));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return auctionServiceValidation.validateId(request.pathVariable("id"))
                .zipWith(ReactiveSecurityContextHolder.getContext()
                        .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString())))
                .zipWith(request.bodyToMono(AuctionUpdateDTO.class)
                        .flatMap(auctionServiceValidation::validateAuctionUpdateDTO))
                .flatMap(tuple -> auctionService.updateAuction(tuple.getT1().getT2(), tuple.getT1().getT1(), tuple.getT2()))
                .flatMap(auctionDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctionDTO));
    }

    public Mono<ServerResponse> getAllAuctionsForUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> auth.getAuthentication().getPrincipal().toString())
                .flatMap(auctionServiceValidation::validateId)
                .flatMap(userId -> auctionService.getAllActiveAuctionsForUser(userId)
                        .collectList()
                        .flatMap(auctions -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctions)));
    }

    public Mono<ServerResponse> getPrivateAuctionsForUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> auth.getAuthentication().getPrincipal().toString())
                .flatMap(auctionServiceValidation::validateId)
                .flatMap(userId -> auctionService.getAllActivePrivateAuctions(userId)
                        .collectList()
                        .flatMap(auctions -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctions)));
    }

    public Mono<ServerResponse> getAllAuctionsForUserSeller(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> auth.getAuthentication().getPrincipal().toString())
                .flatMap(auctionServiceValidation::validateId)
                .flatMap(userId -> auctionService.getAllSellersAuctions(userId)
                        .collectList()
                        .flatMap(auctions -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctions)));
    }
}

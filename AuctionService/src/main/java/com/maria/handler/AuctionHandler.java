package com.maria.handler;

import com.maria.core.entity.AuctionDTO;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import com.maria.service.AuctionService;
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

    public Mono<ServerResponse> create(ServerRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> Long.valueOf(auth.getPrincipal().toString()))
                .zipWith(request.bodyToMono(CreateAuctionRequestDTO.class))
                .flatMap(tuple -> auctionService.createAuction(tuple.getT2(), tuple.getT1()))
                .flatMap(auctionDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctionDTO));
    }

    public Mono<ServerResponse> sendInvitationToUser(ServerRequest request){
        Long auctionId = Long.valueOf(request.pathVariable("auctionId"));

        return request.bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .flatMap(invitedUserEmails ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                                .flatMap(userId -> auctionService.sendInvitation(auctionId, userId, invitedUserEmails))
                                .then(ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue("Invitation sent successfully")));
    }

    public Mono<ServerResponse> removeUserFromAuction(ServerRequest request){
        Long auctionId = Long.valueOf(request.pathVariable("auctionId"));

        return request.bodyToMono(String.class)
                .flatMap(removedUserEmail ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .map(authentication -> Long.valueOf(authentication.getPrincipal().toString()))
                                .flatMap(userId -> auctionService.removeUserFromAuction(removedUserEmail, auctionId, userId))
                                .then(ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue("User removed")));
    }

    public Mono<ServerResponse> getInfo(ServerRequest request){
        Long auctionId = Long.valueOf(request.pathVariable("id"));

        return ReactiveSecurityContextHolder.getContext()
                        .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                                .flatMap(userId -> auctionService.getAuction(auctionId, userId)
                                        .flatMap(auctionDTO ->
                                                ServerResponse
                                                        .ok()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(auctionDTO)));
    }

    public Mono<ServerResponse> getActivePublicAuctions(ServerRequest request){
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(auctionService.getAllActivePublicAuctions(), AuctionDTO.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        Long auctionId = Long.valueOf(request.pathVariable("id"));

        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> auctionService.deleteAuction(auctionId, userId)
                                .then(ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue("Auction successfully deleted")));
    }

    public Mono<ServerResponse> closeAuction(ServerRequest request){
        Long auctionId = Long.valueOf(request.pathVariable("id"));

        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> auctionService.closeAuction(auctionId, userId)
                        .flatMap(auctionDTO ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(auctionDTO)));
    }

    public Mono<ServerResponse> update(ServerRequest request){
        Long auctionId = Long.valueOf(request.pathVariable("id"));

        return request.bodyToMono(AuctionUpdateDTO.class)
                        .flatMap(auctionUpdateDTO ->
                            ReactiveSecurityContextHolder.getContext()
                                    .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                                    .flatMap(userId -> auctionService.updateAuction(userId, auctionId, auctionUpdateDTO))
                                    .flatMap(auctionDTO ->
                                            ServerResponse
                                                    .ok()
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .bodyValue(auctionDTO)));


    }

    public Mono<ServerResponse> getAllAuctionsForUser(ServerRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> auctionService.getAllActiveAuctionsForUser(userId)
                        .collectList()
                        .flatMap(auctions -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctions)));
    }

    public Mono<ServerResponse> getPrivateAuctionsForUser(ServerRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> auctionService.getAllActivePrivateAuctions(userId)
                        .collectList()
                        .flatMap(auctions -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctions)));
    }

    public Mono<ServerResponse> getAllAuctionsForUserSeller(ServerRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> auctionService.getAllSellersAuctions(userId)
                        .collectList()
                        .flatMap(auctions -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(auctions)));
    }
}

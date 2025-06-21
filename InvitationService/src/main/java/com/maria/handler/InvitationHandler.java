package com.maria.handler;

import com.maria.constant.InvitationServiceConstants;
import com.maria.entity.AcceptanceRequest;
import com.maria.service.InvitationService;
import com.maria.validator.InvitationServiceValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class InvitationHandler {
    private final InvitationService invitationService;
    private final InvitationServiceValidation invitationServiceValidation;

    public Mono<ServerResponse> invitationsForUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> invitationService.getInvitationsForUser(userId)
                        .collectList())
                .flatMap(invitations ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(invitations));

    }

    public Mono<ServerResponse> respondToInvitation(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(request.bodyToMono(AcceptanceRequest.class)
                        .flatMap(invitationServiceValidation::validateAcceptanceRequest))
                .flatMap(tuple -> invitationService.respondToInvitation(tuple.getT1(), tuple.getT2().getAuctionId(), tuple.getT2().isAcceptance()))
                .then(ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(InvitationServiceConstants.RESPONSE_BEEN_SENT));
    }
}

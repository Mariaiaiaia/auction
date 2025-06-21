package com.maria.service;

import com.maria.entity.Invitation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvitationService {
    Flux<Invitation> getInvitationsForUser(Long userId);

    Mono<Void> respondToInvitation(Long userId, Long auctionId, boolean accepted);
}

package com.maria.repository;

import com.maria.entity.Invitation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvitationRepository extends R2dbcRepository<Invitation, Long> {
    Flux<Invitation> findByUserId(Long userId);

    Mono<Invitation> findByUserIdAndAuctionId(Long userId, Long auctionId);
}

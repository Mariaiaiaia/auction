package com.maria.core.entity;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvitationEvent {
    private Long auctionId;
    private Long sellerId;
    private Long userId;
}

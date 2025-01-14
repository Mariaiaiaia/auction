package com.maria.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationEvent {
    private Long auctionId;
    private Long sellerId;
    private Long userId;
}

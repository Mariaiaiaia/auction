package com.maria.core.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceEvent {
    private Long auctionId;
    private Long userId;
    private boolean acceptance;
}

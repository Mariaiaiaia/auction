package com.maria.core.entity;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewBitEvent {
    private Long bidId;
    private Long auctionId;
    private BigDecimal bidAmount;
    private Long bidderId;
}

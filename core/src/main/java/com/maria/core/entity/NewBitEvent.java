package com.maria.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NewBitEvent {
    private Long bidId;
    private Long auctionId;
    private BigDecimal bidAmount;
    private Long bidderId;
}

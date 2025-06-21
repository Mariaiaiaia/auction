package com.maria.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BidDTO {
    private Long userId;
    private Long auctionId;
    private BigDecimal bidAmount;
}



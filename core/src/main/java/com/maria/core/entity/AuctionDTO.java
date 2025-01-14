package com.maria.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AuctionDTO {
    private Long auctionId;
    private Long itemId;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private Long sellerId;
    private boolean publicAccess;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
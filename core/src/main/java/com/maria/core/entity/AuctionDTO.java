package com.maria.core.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
package com.maria.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionFinishedNotificationEvent extends Notification{
    private Long itemId;
    private LocalDateTime timestamp;
    private Long auctionId;
    private BigDecimal finalPrice;
}

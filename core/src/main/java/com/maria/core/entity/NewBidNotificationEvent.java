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
public class NewBidNotificationEvent extends Notification{
    private BigDecimal newBid;
    private NotificationType type;
    private Long itemId;
    private LocalDateTime timestamp;
    private Long auctionId;
    private Long bidderId;
}

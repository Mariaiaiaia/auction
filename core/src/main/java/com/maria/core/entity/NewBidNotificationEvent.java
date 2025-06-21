package com.maria.core.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class NewBidNotificationEvent extends Notification{
    private BigDecimal newBid;
    private NotificationType type;
    private Long itemId;
    private Long bidderId;
}

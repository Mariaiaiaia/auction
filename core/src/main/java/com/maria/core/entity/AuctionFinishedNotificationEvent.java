package com.maria.core.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionFinishedNotificationEvent extends Notification{
    private Long itemId;
    private BigDecimal finalPrice;
}

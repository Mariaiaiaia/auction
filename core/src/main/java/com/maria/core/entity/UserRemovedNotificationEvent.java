package com.maria.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRemovedNotificationEvent extends Notification{
    private Long itemId;
    private LocalDateTime timestamp;
    private Long userId;
    private Long auctionId;
}

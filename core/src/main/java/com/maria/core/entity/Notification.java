package com.maria.core.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private LocalDateTime timestamp;
    private Long auctionId;
}

package com.maria.core.entity;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionItemEvent {
    private Long auctionId;
    private Long itemId;
}

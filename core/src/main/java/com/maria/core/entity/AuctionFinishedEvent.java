package com.maria.core.entity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuctionFinishedEvent extends Notification{
    private Long auctionId;
    private Long itemId;
}

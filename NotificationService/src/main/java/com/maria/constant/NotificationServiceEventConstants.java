package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public class NotificationServiceEventConstants {
    public static String NEW_BID = "new-bid-notification-events";
    public static String AUCTION_FINISHED = "auction-finished-notification-events";
    public static String DELETE_AUCTION = "delete-auction-events";
}

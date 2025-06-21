package com.maria.constant;

public final class AuctionServiceRouterConstants {
    public final static String GET_AUCTION = "/auctions/{id}";
    public final static String GET_ALL_AUCTIONS_FOR_USER = "/auctions/my_auctions/";
    public final static String GET_SELLER_ID = "/auctions/get_seller/{id}";
    public final static String GET_PRIVATE_AUCTION = "/auctions/private/";
    public final static String GET_AUCTIONS_FOR_SELLER = "/auctions/sell/";
    public final static String DELETE_AUCTION = "/auctions/{id}";
    public final static String CLOSE_AUCTION = "/auctions/close/{id}";
    public final static String UPDATE_AUCTION = "/auctions/{id}";
    public final static String CREATE_AUCTION = "/auctions";
    public final static String GET_PUBLIC_ACTIVE_AUCTIONS = "/auctions";
    public final static String SEND_INVITATION = "/auctions/invitation/{auctionId}";
}

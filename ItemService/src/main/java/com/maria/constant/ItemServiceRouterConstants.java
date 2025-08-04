package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public final class ItemServiceRouterConstants {
    public static final String GET_ITEM = "/items/{id}";
    public static final String CREATE_ITEM = "/items/create";
    public static final String GET_SELLER_ID = "/items/get_seller/{id}";
}

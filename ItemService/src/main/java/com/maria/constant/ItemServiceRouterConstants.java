package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public class ItemServiceRouterConstants {
    public static String GET_ITEM = "/items/{id}";
    public static String CREATE_ITEM = "/items/create";
    public static String GET_SELLER_ID = "/items/get_seller/{id}";
}

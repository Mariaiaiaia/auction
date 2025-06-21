package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public class NotificationServiceRouterConstants {
    public static String GET_ALL_NOTIFICATIONS = "/notifications/";
    public static String DELETE_NOTIFICATION = "/notifications/{id}";
}

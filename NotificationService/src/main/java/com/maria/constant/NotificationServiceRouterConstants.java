package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public final class NotificationServiceRouterConstants {
    public static final String GET_ALL_NOTIFICATIONS = "/notifications/";
    public static final String DELETE_NOTIFICATION = "/notifications/{id}";
}

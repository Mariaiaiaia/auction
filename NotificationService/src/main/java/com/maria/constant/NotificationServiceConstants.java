package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public class NotificationServiceConstants {
    public static String SUCCESSFULLY_DELETED = "Notification successfully deleted";
    public static String NO_NOTIFICATIONS = "No notifications";
    public static String INVALID_NOTIFICATION_ID_FORMAT = "Invalid notification ID format";
    public static String LOG_ERROR_CONSUMER = "Error in Kafka notification consumer: {}";
    public static String MESSAGE_AUCTION_FINISHED = "The auction is finished: ";
    public static String LOG_NOTIFIC_SAVED = "Notification successfully saved";
    public static String LOG_NOT_SAVED = "Failed to save notification: {}";
    public static String EX_AUCTION_NOT_EXIST = "This auction does not exist";
    public static String MESSAGE_NEW_BID = "The auction have a new bid: ";
    public static String LOG_AUCTION_NOT_FOUNDED = "Auction is not founded: {}";
    public static String LOG_FAIL_GET_AUCTION = "Failed to get the auction: {}";
    public static String EX_NEW_BID = "Failed to save new bid notification";
    public static String LOG_NO_USERS_TO_NOTIFICATION = "No users to send notification for auction: {}";
    public static String EX_SERVICE_ERROR = "Service error occurred";
    public static String EX_NOTIFIC_NOT_EXIST = "Notification does not exist";
    public static String LOG_NOTIFIC_DELETED = "Notification {} deleted successfully";
    public static String LOG_FAIL_DELETE_NOTIFIC = "Failed to delete notification: {}";
    public static String EX_DELETE_NOTIFIC = "Failed to delete notification";
    public static String EX_NOTIFIC_NOT_AVAILABLE = "Notification is not available";
    public static String EX_AUCTION_FINISH = "Failed to save auction finished notification";
}



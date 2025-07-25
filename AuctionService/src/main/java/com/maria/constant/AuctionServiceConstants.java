package com.maria.constant;

public final class AuctionServiceConstants {
    public static final String RESPONSE_INVITATION_SENT = "Invitation sent successfully";
    public static final String AUCTION_DELETED = "Auction successfully deleted";
    public static final String EX_AUCTION_NOT_EXIST = "Auction does not exist";
    public static final String LOG_BID_VALID = "Bid is validate for auction, auction id: {}";
    public static final String LOG_AUCTION_SAVED = "Auction successfully saved, auction id: {}";
    public static final String LOG_ERROR_UPDATING_AUCTION = "Error occurred while updating auction: {}";
    public static final String LOG_UNEXPECTED_ERROR = "Unexpected error: {}";
    public static final String EX_FAIL_UPDATE_AUCTION = "Failed to update auction";
    public static final String EX_SELLER_BID_IN_HIS_AUCTION = "Seller cannot bid in his auction";
    public static final String EX_UNAVAILABLE_AUCTION_FOR_BID = "Unable to bid on unavailable auction";
    public static final String EX_LOW_BID = "Bid amount must be higher than the current highest bid";
    public static final String EX_AUCTION_ITEM_ALREADY_EXISTS = "An auction with this item already exists";
    public static final String LOG_ERROR_SAVING_AUCTION = "Error occurred while saving auction: {}";
    public static final String EX_FAIL_TO_CREATE_AUCTION = "Failed to create auction";
    public static final String EX_TIME_INCORRECT = "The auction time is incorrect";
    public static final String EX_FAIL_SAVE_AUCTION = "Failed to save the auction";
    public static final String EX_SERVICE_ERROR = "Service error occurred";
    public static final String LOG_FAIL_GET_ITEM = "Failed to get the item: {}";
    public static final String EX_ITEM_NOT_BELONG_TO_USER = "This item does not belong to this user";
    public static final String LOG_INVITATION_SENT = "Invitation sent for auction {} to user {}";
    public static final String LOG_FAIL_SEND_INVITATION_DETAILS = "Failed to send invitation for auction {} to user {}";
    public static final String LOG_USER_NOT_FOUND = "User not found for auction {}: userId = {}";
    public static final String LOG_FAIL_SEND_INVITATION_ERROR_MESSAGE = "Error occurred while sending invitation: {}";
    public static final String EX_FAIL_SEND_INVITATION = "Failed to send invitation";
    public static final String EX_AUCTION_NOT_AVAILABLE = "Auction is not available";
    public static final String EX_REDIS_ERROR = "An unexpected error occurred while accessing Redis";
    public static final String LOG_USER_ADDED_TO_AUCTION = "User successfully added to the auction";
    public static final String LOG_ACCEPTANCE_ERROR = "Error during acceptance processing: {}";
    public static final String EX_ACCEPTANCE_ERROR = "Failed to process acceptance";
    public static final String LOG_USER_EMAIL_NOT_FONDED = "User is not founded: {}";
    public static final String LOG_FAIL_GET_USER = "Failed to get the user: {}";
    public static final String LOG_NO_ACCESS_TO_AUCTION = "User: {} does not have access to this auction";
    public static final String EX_USER_NOT_PARTICIPANT = "You are not a participant in this auction";
    public static final String EX_AUCTION_ALREADY_STARTED = "It is not possible to make changes to an auction that has already started";
    public static final String LOG_ERROR_GET_SELLER_ID = "Error occurred while retrieving seller: {}";
    public static final String EX_FAIL_GET_SELLER = "Failed to get seller";
    public static final String LOG_ERROR_GET_PUBLIC_AUCTIONS = "Error occurred while retrieving public auctions: {}";
    public static final String LOG_ERROR_GET_SELLERS_AUCTIONS = "Error occurred while retrieving seller's auctions: {}";
    public static final String LOG_ERROR_GET_ACTIVE_AUCTIONS = "Error occurred while retrieving active auctions: {}";
    public static final String LOG_AUCTION_CLOSED = "Auction {} successfully closed";
    public static final String LOG_FAIL_CLOSE_AUCTION = "Failed to close auction with ID {}: {}";
    public static final String EX_FAIL_CLOSE_AUCTION = "Failed to close auction";
    public static final String EX_FAIL_DELETE_AUCTION = "Failed to delete auction";
    public static final String LOG_AUCTION_DELETED = "Auction successfully deleted";
    public static final String LOG_FAIL_DELETE_AUCTION = "Failed to delete auction with ID {}: {}";
    public static final String LOG_AUCTION_CACHED = "Auctions successfully cached";
    public static final String LOG_ERROR_CACHING_AUCTION = "Error while caching auctions: {}";
    public static final String LOG_EXPIRED_AUCTIONS_PROCESSED = "Expired auctions successfully processed";
    public static final String LOG_ERROR_PROCESS_EXPIRED_AUCTIONS = "Error while processing expired auctions: ";
    public static final String LOG_NO_AUCTIONS_ENDING_SOON = "No auctions ending soon found.";
    public static final String EX_ERROR_CACHING_AUCTIONS = "Error while caching auctions";
    public static final String EX_FAIL_GET_AUCTION_IN_CACHE = "Failed to get auction in cache";
    public static final String LOG_ERROR_GET_AUCTION_FROM_CACHE = "Error occurred while retrieving auction: {} from cache: {}";
    public static final String LOG_ERROR_SETTING_AUCTION_FINISHED = "Error occurred while setting auction as finished";
    public static final String LOG_AUCTION_SET_FINISHED = "Auction {} marked as finished in DB";
    public static final String LOG_FAIL_SEND_NOTIFICATION = "Failed to send notification for auction {}: {}";
    public static final String LOG_FAIL_REMOVE_AUCTION_FROM_CACHE = "Failed to remove auction {} from cache";
    public static final String EX_FAIL_REMOVE_AUCTION_FROM_CACHE = "Failed to remove auction from cache";
    public static final String LOG_ERROR_REMOVING_AUCTION_FROM_CACHE = "Error occurred while removing auction {} from cache: {}";
    public static final String LOG_ERROR_CHECK_IF_USER_SEE_AUCTION = "Error occurred while checking if user {} can see auction {}: {}";
    public static final String EX_FAIL_TO_CHECK_USER = "Failed to check user";
    public static final String LOG_ERROR_SAVING_USER_TO_AUCTION = "Error occurred while saving user {} to auction {}: {}";
    public static final String EX_FAIL_TO_SAVE_USER = "Failed to save user";
    public static final String LOG_BID_EVENT_NEW_BID = "Bid event: new bid: {} from user: {}";
    public static final String LOG_ERROR_KAFKA_BID_CONSUMER = "Error in Kafka bid consumer: {}";
    public static final String LOG_ACCEPTANCE_EVENT = "Acceptance event: from user: {}";
    public static final String LOG_ERROR_KAFKA_ACCEPTANCE_CONSUMER = "Error in Kafka acceptances consumer: ";
    public static final String LOG_NOTIFICATION_SENT_TO_TOPIC = "Notification sent successfully to topic {}";
    public static final String LOG_FAIL_SEND_NOTIFICATION_FOR_TOPIC = "Failed to send notification for topic {}: {}";
    public static final String LOG_INVITATION_EVENT_SENT = "Invitation event sent successfully for auction {} to user {}";
    public static final String LOG_FAIL_SEND_INVITATION = "Failed to send invitation for auction {} to user {}: {}";
    public static final String LOG_AUCTION_CREATED_EVENT_SENT = "Auction created event sent successfully for auction {}";
    public static final String LOG_FAIL_SEND_AUCTION_CREATED_EVENT = "Failed to send auction created event for auction {}";
    public static final String LOG_AUCTION_REMOVED_EVENT_SENT = "Auction removed event sent successfully for auction {}";
    public static final String LOG_FAIL_SEND_AUCTION_REMOVED_EVENT = "Failed to send auction removed event for auction {}";
    public static final String EX_INVALID_ID_FORMAT = "Invalid user ID format";
    public static final String EX_ID_POSITIVE_NUMBER = "User ID must be not null and positive number";
    public static final String VALIDATION_STARTING_PRICE = "Starting price must be greater than 0";
    public static final String VALIDATION_START_DATE = "Start date must be in the future";
    public static final String VALIDATION_END_DATE = "End date must be in the future";
    public static final String VALIDATION_ITEM_REQ = "Item is required";
    public static final String VALIDATION_PRICE_REQ = "Starting price is required";
    public static final String VALIDATION_START_DATE_REQ = "Start date is required";
    public static final String VALIDATION_END_DATE_REQ = "End date is required";
}

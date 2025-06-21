package com.maria.constant;

public final class BidServiceConstants {
    public final static String VALIDATION_BID_VALUE = "Bid amount must be greater than 0";
    public final static String VALIDATION_BID_VALUE_REQ = "Bid amount is required";
    public final static String VALIDATION_AUCTION_ID_REQ = "Auction is required";
    public final static String EX_ID_POSITIVE_NUMBER = "ID must be not null and positive number";
    public final static String EX_INVALID_ID_FORMAT = "Invalid ID format";
    public final static String RESPONSE_BID_PLACED = "Your bid has been placed successfully";
    public final static String LOG_ERROR_KAFKA_CONSUMER = "Error in Kafka consumer: {}";
    public final static String LOG_BID_SAVED = "Bid successfully saved, auction id: {}, bid amount: {}";
    public final static String EX_FAIL_TO_GET_BIDS = "Failed to get bid";
    public final static String EX_FAIL_TO_CREATE_BID = "Failed to create bid";
    public final static String LOG_FAIL_TO_SAVE_BID = "Failed to save bid: {}";
    public final static String EX_FAIL_TO_SAVE_BID = "Failed to save bid";
    public final static String LOG_ERROR_WHILE_RETRIEVING_BIDS = "Error occurred while retrieving bids: {}";
    public final static String EX_AUCTION_NOT_AVAILABLE = "This auction is not available";
    public final static String LOG_FAIL_TO_GET_AUCTION = "Failed to get the auction: {}";
    public final static String EX_SERVICE_ERROR = "Service error occurred";
    public final static String LOG_FAIL_TO_DELETE_BID = "Error occurred while deleting the bid: {}";
    public final static String EX_FAIL_TO_DELETE_BID = "Failed to delete bid";
    public final static String EX_BID_EXISTS = "A bid for this auction already exists";
    public final static String EX_BIDS_AVAILABLE = "Only the seller can see the bids for his auction";
}

package com.maria.constant;

public final class InvitationServiceConstants {
    public final static String LOG_ERROR_RETRIEVING_INVITATIONS = "Error occurred while retrieving user invitations: {}";
    public final static String EX_FAIL_GET_INVITATIONS = "Failed to get invitations";
    public final static String LOG_NEW_INVITATION = "Invitation event: auction: {} user: {}";
    public final static String LOG_ERROR_CONSUMER = "Error in Kafka consumer: {}";
    public final static String LOG_INVITATION_SAVED = "Invitation successfully saved, invitation id: {}";
    public final static String LOG_FAIL_SAVE_INVITATION = "Failed to save invitation: {}";
    public final static String EX_FAIL_SAVE_INVITATION = "Failed to save invitation";
    public final static String LOG_ACCEPTANCE_SENT = "Acceptance sent successfully";
    public final static String LOG_ERROR_PRODUCER = "Error in Kafka producer: {}";
    public final static String LOG_INVITATION_UPDATED = "Invitation successfully updated";
    public final static String LOG_FAIL_UPDATE_INVITATION = "Failed to update acceptance: {}";
    public final static String EX_FAIL_UPDATE_INVITATION = "Failed to update acceptance";
    public final static String RESPONSE_BEEN_SENT = "The response to the invitation has been sent";
    public final static String VALID_AUCTION_ID_REQ = "Auction id is required";
    public final static String VALID_RESPONSE_REQ = "Response value is required";
    public final static String LOG_NO_INVITATIONS = "No invitations found for user {}";
    public final static String EX_INVITATION_NOT_FOUND = "Invitation not found";
}

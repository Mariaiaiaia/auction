package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public final class ItemServiceConstants {
    public static final String FILE_MISSING = "File is missing";
    public static final String EX_ITEM_NOT_EXIST = "Item does not exist";
    public static final String EX_FAIL_GET_ITEM = "Failed to get item";
    public static final String LOG_ERROR_RETRIEVING_ITEM = "Error occurred while retrieving item: {}";
    public static final String LOG_ERROR_CREATING_ITEM = "Error while creating item: {}";
    public static final String EX_FAIL_CREATE_ITEM = "Failed to create item";
    public static final String EX_ITEM_ID_POSITIVE_NUMBER = "Item ID must be not null and positive number";
    public static final String EX_INVALID_ITEM_ID_FORMAT = "Invalid item ID format";
    public static final String EX_ITEM_NAME_REQ = "Item name is required";
    public static final String EX_ITEM_DESCRIPTION_REQ = "Item description is required";
}

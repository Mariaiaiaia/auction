package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public class ItemServiceConstants {
    public static String FILE_MISSING = "File is missing";
    public static String EX_ITEM_NOT_EXIST = "Item does not exist";
    public static String EX_FAIL_GET_ITEM = "Failed to get item";
    public static String LOG_ERROR_RETRIEVING_ITEM = "Error occurred while retrieving item: {}";
    public static String LOG_ERROR_CREATING_ITEM = "Error while creating item: {}";
    public static String EX_FAIL_CREATE_ITEM = "Failed to create item";
    public static String EX_ITEM_ID_POSITIVE_NUMBER = "Item ID must be not null and positive number";
    public static String EX_INVALID_ITEM_ID_FORMAT = "Invalid item ID format";
}

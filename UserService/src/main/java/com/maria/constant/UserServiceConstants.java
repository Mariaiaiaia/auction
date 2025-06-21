package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public class UserServiceConstants {
    public static final String EMAIL_EXISTS = "A user with this email already exists";
    public static final String SUCCESSFULLY_REGISTERED = "User successfully registered";
    public static final String FAILED_DELETE = "Failed to delete user";
    public static final String UPDATED_FAILED = "User updated failed";
    public static final String NOT_EXIST = "User does not exist";
    public static final String NOT_AVAILABLE = "User is not available";
    public static final String DATABASE_ERROR = "Database error";
    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String USER_ID_POSITIVE_NUMBER = "User ID must be not null and positive number";
    public static final String INVALID_USER_ID_FORMAT = "Invalid user ID format";
    public static final String LOG_FAILED_GET_USER = "Failed to get the user: {}";
    public static final String LOG_REGISTR_ERROR = "Error during user registration: {}";
    public static final String LOG_UPDATE_SUCCESS = "User {} updated successfully";
    public static final String LOG_UPDATE_ERROR = "Error updating user data: {}";
    public static final String LOG_DELETE_SUCCESS = "User {} successfully deleted";
    public static final String LOG_DELETE_ERROR = "Failed to delete user: {}";
    public static final String VALID_FIRST_NAME_REQ = "First name is required";
    public static final String VALID_LAST_NAME_REQ = "Last name is required";
    public static final String VALID_PASS_REQ = "Password is required";
    public static final String VALID_PASS = "Password must be between 5 and 35 characters";
    public static final String VALID_EMAIL = "Email should be valid";
}

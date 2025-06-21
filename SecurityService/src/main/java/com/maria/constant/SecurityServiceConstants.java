package com.maria.constant;

import org.springframework.stereotype.Component;

@Component
public class SecurityServiceConstants {
    public static final String PASS_NOT_MATCH = "Password does not match";
    public static final String LOGIN_FAILED = "Login failed";
    public static final String LOG_LOGIN_FAILED = "Login failed: {}";
    public static final String VALID_EMAIL = "Email should be valid";
    public static final String VALID_PASS_REQ = "Password is required";
    public static final String VALID_PASS = "Password must be between 5 and 35 characters";
}

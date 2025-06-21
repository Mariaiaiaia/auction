package com.maria.dto;

import com.maria.constant.UserServiceConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {
    @NotEmpty(message = UserServiceConstants.VALID_FIRST_NAME_REQ)
    private String firstName;
    @NotEmpty(message = UserServiceConstants.VALID_LAST_NAME_REQ)
    private String lastName;
    @NotEmpty(message = UserServiceConstants.VALID_PASS_REQ)
    @Size(min = 5, max = 35, message
            = UserServiceConstants.VALID_PASS)
    private String password;
    @Email(message = UserServiceConstants.VALID_EMAIL)
    private String email;
}

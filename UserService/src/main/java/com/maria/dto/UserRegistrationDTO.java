package com.maria.dto;

import com.maria.constant.UserServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for user registration")
public class UserRegistrationDTO {
    @NotEmpty(message = UserServiceConstants.VALID_FIRST_NAME_REQ)
    @Schema(description = "First name of the user", example = "Harry")
    private String firstName;
    @NotEmpty(message = UserServiceConstants.VALID_LAST_NAME_REQ)
    @Schema(description = "Last name of the user", example = "Potter")
    private String lastName;
    @NotEmpty(message = UserServiceConstants.VALID_PASS_REQ)
    @Size(min = 5, max = 35, message = UserServiceConstants.VALID_PASS)
    @Schema(description = "User password (min 5 characters, max 35)", example = "Expelliarmus123")
    private String password;
    @Email(message = UserServiceConstants.VALID_EMAIL)
    @Schema(description = "User email address", example = "harry.potter@hogwarts.edu")
    private String email;
}

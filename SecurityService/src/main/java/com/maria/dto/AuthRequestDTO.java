package com.maria.dto;

import com.maria.constant.SecurityServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for user authentication (login)")
public class AuthRequestDTO {
    @Schema(description = "User's email address", example = "harry.potter@hogwarts.com")
    @Email(message = SecurityServiceConstants.VALID_EMAIL)
    private String email;
    @Schema(description = "User's password", example = "expelliarmus123", minLength = 5, maxLength = 35)
    @NotEmpty(message = SecurityServiceConstants.VALID_PASS_REQ)
    @Size(min = 5, max = 35, message
            = SecurityServiceConstants.VALID_PASS)
    private String password;
}

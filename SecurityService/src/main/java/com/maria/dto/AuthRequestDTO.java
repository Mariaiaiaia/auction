package com.maria.dto;

import com.maria.constant.SecurityServiceConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {
    @Email(message = SecurityServiceConstants.VALID_EMAIL)
    private String email;
    @NotEmpty(message = SecurityServiceConstants.VALID_PASS_REQ)
    @Size(min = 5, max = 35, message
            = SecurityServiceConstants.VALID_PASS)
    private String password;
}

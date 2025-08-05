package com.maria.dto;

import com.maria.constant.UserServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for updating a user")
public class UserUpdateDTO {
    @Schema(description = "New first name", example = "Harry")
    private String firstName;
    @Schema(description = "New last name", example = "Potter")
    private String lastName;
    @Size(min = 5, max = 35, message = UserServiceConstants.VALID_PASS)
    @Schema(description = "New email address", example = "harry.potter@hogwarts.edu")
    private String password;
}

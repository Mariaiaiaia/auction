package com.maria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a user")
public class UserDTO {
    @Schema(description = "Unique identifier of the user", example = "7")
    private Long userId;
    @Schema(description = "User's first name", example = "Harry")
    private String firstName;
    @Schema(description = "User's last name", example = "Potter")
    private String lastName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "User's password (write-only)", example = "Expelliarmus123!", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
    @Schema(description = "User's email address", example = "harry.potter@hogwarts.edu")
    private String email;
}

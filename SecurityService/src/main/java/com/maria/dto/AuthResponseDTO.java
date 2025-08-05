package com.maria.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details about the JWT token")
public class AuthResponseDTO {
    @Schema(description = "ID of the authenticated user", example = "123")
    private Long userId;
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    @Schema(description = "Token issuance date and time", example = "2025-08-04T12:00:00Z")
    private Date issuedAt;
    @Schema(description = "Token expiration date and time", example = "2025-08-04T13:00:00Z")
    private Date expiresAt;
}

package com.maria.dto;

import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private Long userId;
    private String token;
    private Date issuedAt;
    private Date expiresAt;
}

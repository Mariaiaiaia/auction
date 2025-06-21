package com.maria.entity;

import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenDetails {
    private Long userId;
    private String token;
    private Date issuedAt;
    private Date expiresAt;
}

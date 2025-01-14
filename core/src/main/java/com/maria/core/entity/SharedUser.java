package com.maria.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedUser {
    private Long userId;
    private String password;
    private String email;
    private String role;
}



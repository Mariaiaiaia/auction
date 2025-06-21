package com.maria.core.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SharedUser {
    private Long userId;
    private String password;
    private String email;
    private String role;
}

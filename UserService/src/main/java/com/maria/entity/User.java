package com.maria.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("usr")
public class User {
    @Id
    @Column("id")
    private Long userId;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String role;
}
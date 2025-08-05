package com.maria.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "User entity representing user information")
public class User {
    @Id
    @Column("id")
    @Schema(description = "Unique ID of the user", example = "101")
    private Long userId;
    @Schema(description = "First name of the user", example = "Harry")
    private String firstName;
    @Schema(description = "Last name of the user", example = "Potter")
    private String lastName;
    @Schema(description = "User's password (write-only)", example = "Expelliarmus123")
    private String password;
    @Schema(description = "Email address of the user", example = "harry.potter@hogwarts.edu")
    private String email;
    @Schema(description = "Role of the user", example = "USER")
    private String role;
}
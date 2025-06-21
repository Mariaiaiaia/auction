package com.maria.dto;

import com.maria.constant.UserServiceConstants;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    @Size(min = 5, max = 35, message
            = UserServiceConstants.VALID_PASS)
    private String password;
}

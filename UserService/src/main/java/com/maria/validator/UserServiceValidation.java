package com.maria.validator;

import com.maria.constant.UserServiceConstants;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.exception.InvalidEmailException;
import com.maria.exception.InvalidUserIdException;
import com.maria.exception.InvalidUserRegistrationDTOException;
import com.maria.exception.InvalidUserUpdateDTOException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UserServiceValidation {
    private final Validator validator;

    public Mono<String> validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            return Mono.error(new InvalidEmailException(UserServiceConstants.INVALID_EMAIL_FORMAT));
        }
        return Mono.just(email);
    }

    public Mono<Long> validateUserId(String id) {
        try {
            long userId = Long.parseLong(id);
            if (userId < 0) {
                return Mono.error(new InvalidUserIdException(UserServiceConstants.USER_ID_POSITIVE_NUMBER));
            }
            return Mono.just(userId);
        } catch (NumberFormatException ex) {
            return Mono.error(new InvalidUserIdException(UserServiceConstants.INVALID_USER_ID_FORMAT));
        }
    }

    public Mono<UserRegistrationDTO> validateUserRegistrationDTO(UserRegistrationDTO userRegistrationDTO) {
        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(userRegistrationDTO);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new InvalidUserRegistrationDTOException(errorMessage));
        }
        return Mono.just(userRegistrationDTO);
    }

    public Mono<UserUpdateDTO> validateUserUpdateDTO(UserUpdateDTO userUpdateDTO) {
        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(userUpdateDTO);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new InvalidUserUpdateDTOException(errorMessage));
        }
        return Mono.just(userUpdateDTO);
    }
}

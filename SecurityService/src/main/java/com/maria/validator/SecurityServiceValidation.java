package com.maria.validator;

import com.maria.dto.AuthRequestDTO;
import com.maria.exception.InvalidAuthRequestDTOException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SecurityServiceValidation {
    private final Validator validator;

    public Mono<AuthRequestDTO> validateAuthRequestDTO(AuthRequestDTO authRequestDTO) {
        Set<ConstraintViolation<AuthRequestDTO>> violations = validator.validate(authRequestDTO);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new InvalidAuthRequestDTOException(errorMessage));
        }
        return Mono.just(authRequestDTO);
    }
}

package com.maria.validator;

import com.maria.entity.AcceptanceRequest;
import com.maria.exception.InvalidAcceptanceRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class InvitationServiceValidation {
    private final Validator validator;

    public Mono<AcceptanceRequest> validateAcceptanceRequest(AcceptanceRequest acceptanceRequest) {
        Set<ConstraintViolation<AcceptanceRequest>> violations = validator.validate(acceptanceRequest);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new InvalidAcceptanceRequestException(errorMessage));
        }
        return Mono.just(acceptanceRequest);
    }
}

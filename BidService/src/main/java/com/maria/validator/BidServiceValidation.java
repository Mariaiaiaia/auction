package com.maria.validator;

import com.maria.constant.BidServiceConstants;
import com.maria.dto.PlaceBidRequest;
import com.maria.exception.InvalidIdException;
import com.maria.exception.InvalidPlaceBidRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BidServiceValidation {
    private final Validator validator;

    public Mono<Long> validateId(String id) {
        try {
            long auctionId = Long.parseLong(id);
            if (auctionId < 0) {
                return Mono.error(new InvalidIdException(BidServiceConstants.EX_ID_POSITIVE_NUMBER));
            }
            return Mono.just(auctionId);
        } catch (NumberFormatException ex) {
            return Mono.error(new InvalidIdException(BidServiceConstants.EX_INVALID_ID_FORMAT));
        }
    }

    public Mono<PlaceBidRequest> validatePlaceBidRequest(PlaceBidRequest placeBidRequest) {
        Set<ConstraintViolation<PlaceBidRequest>> violations = validator.validate(placeBidRequest);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new InvalidPlaceBidRequestException(errorMessage));
        }
        return Mono.just(placeBidRequest);
    }
}

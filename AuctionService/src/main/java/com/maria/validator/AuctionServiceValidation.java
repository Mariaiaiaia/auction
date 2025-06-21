package com.maria.validator;

import com.maria.constant.AuctionServiceConstants;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import com.maria.exception.InvalidAuctionUpdateDTOException;
import com.maria.exception.InvalidCreateAuctionRequestDTOException;
import com.maria.exception.InvalidIdException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AuctionServiceValidation {
    private final Validator validator;

    public Mono<Long> validateId(String id) {
        try {
            long userId = Long.parseLong(id);
            if (userId < 0) {
                return Mono.error(new InvalidIdException(AuctionServiceConstants.EX_ID_POSITIVE_NUMBER));
            }
            return Mono.just(userId);
        } catch (NumberFormatException ex) {
            return Mono.error(new InvalidIdException(AuctionServiceConstants.EX_INVALID_ID_FORMAT));
        }
    }

    public Mono<CreateAuctionRequestDTO> validateCreateAuctionRequestDTO(CreateAuctionRequestDTO createAuctionRequestDTO) {
        Set<ConstraintViolation<CreateAuctionRequestDTO>> violations = validator.validate(createAuctionRequestDTO);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new InvalidCreateAuctionRequestDTOException(errorMessage));
        }
        return Mono.just(createAuctionRequestDTO);
    }

    public Mono<AuctionUpdateDTO> validateAuctionUpdateDTO(AuctionUpdateDTO auctionUpdateDTO) {
        Set<ConstraintViolation<AuctionUpdateDTO>> violations = validator.validate(auctionUpdateDTO);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new InvalidAuctionUpdateDTOException(errorMessage));
        }
        return Mono.just(auctionUpdateDTO);
    }
}

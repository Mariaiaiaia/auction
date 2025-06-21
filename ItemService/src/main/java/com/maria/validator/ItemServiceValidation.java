package com.maria.validator;

import com.maria.constant.ItemServiceConstants;
import com.maria.exception.InvalidItemIdException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ItemServiceValidation {
    private final Validator validator;

    public Mono<Long> validateItemId(String id) {
        try {
            long userId = Long.parseLong(id);
            if (userId < 0) {
                return Mono.error(new InvalidItemIdException(ItemServiceConstants.EX_ITEM_ID_POSITIVE_NUMBER));
            }
            return Mono.just(userId);
        } catch (NumberFormatException ex) {
            return Mono.error(new InvalidItemIdException(ItemServiceConstants.EX_INVALID_ITEM_ID_FORMAT));
        }
    }
}

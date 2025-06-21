package com.maria.validator;

import com.maria.constant.NotificationServiceConstants;
import com.maria.exception.InvalidNotificationIdException;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class NotificationServiceValidation {
    public Mono<String> validateNotificationId(String id) {
        if (!ObjectId.isValid(id)) {
            return Mono.error(new InvalidNotificationIdException(NotificationServiceConstants.INVALID_NOTIFICATION_ID_FORMAT));
        }
        return Mono.just(id);
    }
}

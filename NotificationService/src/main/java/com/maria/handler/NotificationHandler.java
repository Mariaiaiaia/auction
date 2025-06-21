package com.maria.handler;

import com.maria.constant.NotificationServiceConstants;
import com.maria.service.NotificationService;
import com.maria.validator.NotificationServiceValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class NotificationHandler {
    private final NotificationService notificationService;
    private final NotificationServiceValidation notificationServiceValidation;

    public Mono<ServerResponse> getNotifications(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> notificationService.getNotificationForUser(userId)
                        .collectList()
                        .flatMap(notifications -> {
                            if (notifications.isEmpty()) {
                                return ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(NotificationServiceConstants.NO_NOTIFICATIONS);
                            }

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(notifications);
                        }));
    }

    public Mono<ServerResponse> deleteNotification(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(notificationServiceValidation.validateNotificationId(request.pathVariable("id")))
                .flatMap(tuple -> notificationService.deleteNotificationForUser(tuple.getT2(), tuple.getT1())
                        .then(ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(NotificationServiceConstants.SUCCESSFULLY_DELETED)));
    }
}

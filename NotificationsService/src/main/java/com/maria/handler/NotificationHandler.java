package com.maria.handler;

import com.maria.service.NotificationService;
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

    public Mono<ServerResponse> getNotifications(ServerRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> notificationService.getNotificationForUser(userId)
                        .collectList()
                        .flatMap(notifications -> {

                            if(notifications.isEmpty()){
                                return ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue("No notifications");
                            }

                            return ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(notifications);}));
    }

    public Mono<ServerResponse> deleteNotification(ServerRequest request){
        String notificationId = request.pathVariable("id");

        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> notificationService.deleteNotificationForUser(notificationId, userId)
                        .then(ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue("Notification successfully deleted")));
    }
}

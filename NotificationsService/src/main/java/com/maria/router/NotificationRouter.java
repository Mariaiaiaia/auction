package com.maria.router;

import com.maria.handler.NotificationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class NotificationRouter {

    @Bean
    RouterFunction<ServerResponse> router(NotificationHandler notificationHandler){
        return RouterFunctions
                .route(GET("/notifications/").and(accept(MediaType.APPLICATION_JSON)), notificationHandler::getNotifications)
                .andRoute(DELETE("/notifications/{id}").and(accept(MediaType.APPLICATION_JSON)), notificationHandler::deleteNotification);
    }
}

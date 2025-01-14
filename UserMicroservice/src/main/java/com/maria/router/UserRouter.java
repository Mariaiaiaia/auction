package com.maria.router;

import com.maria.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class UserRouter {
    @Bean
    RouterFunction<ServerResponse> route(UserHandler userHandler) {
        return RouterFunctions
                .route(GET("/users/{id}").and(accept(MediaType.APPLICATION_JSON)), userHandler::getInfo)
                .andRoute(PATCH("/users/{id}").and(accept(MediaType.APPLICATION_JSON)), userHandler::update)
                .andRoute(DELETE("/users/{id}").and(accept(MediaType.APPLICATION_JSON)), userHandler::delete)
                .andRoute(GET("/users/find_user/{userEmail}").and(accept(MediaType.APPLICATION_JSON))
                        .and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))),
                        userHandler::findUserByEmail)
                .andRoute(POST("/users/register").and(accept(MediaType.APPLICATION_JSON)), userHandler::register);
    }
}
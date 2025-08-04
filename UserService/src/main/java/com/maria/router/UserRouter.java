package com.maria.router;

import com.maria.constant.UserServiceRouterConstants;
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
    public RouterFunction<ServerResponse> route(UserHandler userHandler) {
        return RouterFunctions
                .route(GET(UserServiceRouterConstants.GET_INFO).and(accept(MediaType.APPLICATION_JSON)), userHandler::getInfo)
                .andRoute(PATCH(UserServiceRouterConstants.UPDATE_USER).and(accept(MediaType.APPLICATION_JSON)), userHandler::update)
                .andRoute(DELETE(UserServiceRouterConstants.DELETE_USER).and(accept(MediaType.APPLICATION_JSON)), userHandler::delete)
                .andRoute(GET(UserServiceRouterConstants.FIND_USER_BY_EMAIL).and(accept(MediaType.APPLICATION_JSON))
                                .and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))), userHandler::findUserByEmail)
                .andRoute(POST(UserServiceRouterConstants.REGISTER).and(accept(MediaType.APPLICATION_JSON)), userHandler::register);
    }
}
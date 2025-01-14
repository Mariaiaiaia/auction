package com.maria.router;

import com.maria.handler.SecurityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class SecurityRouter {

    @Bean
    RouterFunction<ServerResponse> route(SecurityHandler securityHandler) {
        return RouterFunctions
                .route(POST("/api/login").and(accept(MediaType.APPLICATION_JSON)), securityHandler::login)
                .andRoute(POST("/api/logout").and(accept(MediaType.APPLICATION_JSON)), securityHandler::logout);
    }
}
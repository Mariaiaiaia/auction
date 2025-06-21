package com.maria.router;

import com.maria.constant.InvitationServiceRouterConstants;
import com.maria.handler.InvitationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class InvitationRouter {
    @Bean
    RouterFunction<ServerResponse> router(InvitationHandler invitationHandler) {
        return RouterFunctions
                .route(GET(InvitationServiceRouterConstants.INVITATIONS).and(accept(MediaType.APPLICATION_JSON)),
                        invitationHandler::invitationsForUser)
                .andRoute(POST(InvitationServiceRouterConstants.INVITATIONS_ACCEPTANCE).and(accept(MediaType.APPLICATION_JSON)),
                        invitationHandler::respondToInvitation);
    }
}

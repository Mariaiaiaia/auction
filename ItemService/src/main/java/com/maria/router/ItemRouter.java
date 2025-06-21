package com.maria.router;

import com.maria.constant.ItemServiceRouterConstants;
import com.maria.handler.ItemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Component
public class ItemRouter {
    @Bean
    RouterFunction<ServerResponse> router(ItemHandler itemHandler) {
        return RouterFunctions
                .route(GET(ItemServiceRouterConstants.GET_ITEM).and(accept(MediaType.APPLICATION_JSON)), itemHandler::getItem)
                .andRoute(POST(ItemServiceRouterConstants.CREATE_ITEM).and(accept(MediaType.MULTIPART_FORM_DATA)), itemHandler::createItem)
                .andRoute(GET(ItemServiceRouterConstants.GET_SELLER_ID).and(accept(MediaType.APPLICATION_JSON))
                        .and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))), itemHandler::getSellerId);
    }
}

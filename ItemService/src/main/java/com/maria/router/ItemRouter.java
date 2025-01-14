package com.maria.router;

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
    RouterFunction<ServerResponse> router(ItemHandler itemHandler){
        return RouterFunctions
                .route(GET("items/{id}").and(accept(MediaType.APPLICATION_JSON)), itemHandler::getItem)
                .andRoute(POST("items/create").and(accept(MediaType.MULTIPART_FORM_DATA)), itemHandler::createItem);
    }

}

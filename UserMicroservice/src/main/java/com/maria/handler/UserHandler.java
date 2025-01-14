package com.maria.handler;

import com.maria.core.entity.SharedUser;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserHandler {
    private final UserService userService;


    public Mono<ServerResponse> getInfo(ServerRequest request){
        return userService.getUserInfo(Long.valueOf(request.pathVariable("id")))
                .flatMap(userDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(userDTO));
    }


    public Mono<ServerResponse> findUserByEmail(ServerRequest request){
        return userService.getUserByEmail(request.pathVariable("userEmail"))
                .flatMap(user -> {
                    SharedUser authUser = new SharedUser(user.getUserId(), user.getPassword(), user.getEmail(), user.getRole());

                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(authUser);
                });
    }


    public Mono<ServerResponse> register(ServerRequest request){
        return request.bodyToMono(UserRegistrationDTO.class)
                        .flatMap(userService::registerUser)
                .flatMap(user -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user));
    }


    public Mono<ServerResponse> delete(ServerRequest request){
        Long userId = Long.valueOf(request.pathVariable("id"));

        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(currentUserId -> userService.deleteUser(userId, currentUserId))
                        .then(ServerResponse
                                .noContent()
                                .build());
    }


    public Mono<ServerResponse> update(ServerRequest request){
        Long userId = Long.valueOf(request.pathVariable("id"));

        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(request.bodyToMono(UserUpdateDTO.class))
                .flatMap(tuple -> userService.updateUser(tuple.getT2(), userId, tuple.getT1()))
                .flatMap(user -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user));
    }
}


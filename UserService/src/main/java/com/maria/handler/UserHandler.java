package com.maria.handler;

import com.maria.core.entity.SharedUser;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.service.UserService;
import com.maria.validator.UserServiceValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {
    private final UserService userService;
    private final UserServiceValidation userServiceValidation;

    public Mono<ServerResponse> getInfo(ServerRequest request) {
        return userServiceValidation.validateUserId(request.pathVariable("id"))
                .flatMap(userService::getUserInfo)
                .flatMap(userDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(userDTO));
    }

    public Mono<ServerResponse> findUserByEmail(ServerRequest request) {
        return userServiceValidation.validateEmail(request.pathVariable("userEmail"))
                .flatMap(userService::getUserByEmail)
                .flatMap(user -> {
                    SharedUser authUser = new SharedUser(user.getUserId(), user.getPassword(), user.getEmail(), user.getRole());

                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(authUser);
                });
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(UserRegistrationDTO.class)
                .flatMap(userServiceValidation::validateUserRegistrationDTO)
                .flatMap(userService::registerUser)
                .flatMap(user -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .zipWith(userServiceValidation.validateUserId(request.pathVariable("id")))
                .flatMap(tuple -> userService.deleteUser(tuple.getT2(), tuple.getT1()))
                .then(ServerResponse
                        .ok()
                        .build());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return userServiceValidation.validateUserId(request.pathVariable("id"))
                .zipWith(ReactiveSecurityContextHolder.getContext()
                        .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString())))
                .zipWith(request.bodyToMono(UserUpdateDTO.class)
                        .flatMap(userServiceValidation::validateUserUpdateDTO))
                .flatMap(tuple -> userService.updateUser(tuple.getT2(), tuple.getT1().getT1(), tuple.getT1().getT2()))
                .flatMap(user -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user));
    }
}
















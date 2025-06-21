package com.maria.service;

import com.maria.dto.UserDTO;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.entity.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDTO> getUserInfo(Long userId);

    Mono<User> getUserByEmail(String email);

    Mono<UserDTO> registerUser(UserRegistrationDTO userRegistrationDTO);

    Mono<UserDTO> updateUser(UserUpdateDTO userUpdateDTO, Long userId, Long currentUserId);

    Mono<Void> deleteUser(Long userId, Long currentUserId);
}

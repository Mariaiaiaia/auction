package com.maria.service;

import com.maria.dto.UserDTO;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.entity.User;
import com.maria.exception.DatabaseOperationException;
import com.maria.exception.UserAlreadyExistsException;
import com.maria.exception.UserNotAvailableException;
import com.maria.exception.UserNotExistException;
import com.maria.mapper.UserMapper;
import com.maria.repository.UserRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
 
@Slf4j
@Data
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public Mono<UserDTO> getUserInfo(Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotExistException("User does not exist, with ID: " + userId)))
                .map(userMapper::toDto);
    }

    @Override
    public Mono<User> getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotExistException("User does not exist, with email: " + email)));
    }

    @Override
    public Mono<UserDTO> registerUser(UserRegistrationDTO userRegistrationDTO){
        User newUser = User.builder()
                .firstName(userRegistrationDTO.getFirstName())
                .lastName(userRegistrationDTO.getLastName())
                .email(userRegistrationDTO.getEmail())
                .password(passwordEncoder.encode(userRegistrationDTO.getPassword()))
                .role("user")
                .build();

        return getUserByEmail(userRegistrationDTO.getEmail())
                .flatMap(existingUser -> Mono.<User>error(new UserAlreadyExistsException("A user with this email already exists")))
                .switchIfEmpty(userRepository.save(newUser))
                .doOnSuccess(success -> log.info("User successfully registered"))
                .onErrorResume(ex -> Mono.error(new DatabaseOperationException("User registration failed")))
                .map(userMapper::toDto);
    }

    @Override
    public Mono<UserDTO> updateUser(UserUpdateDTO userUpdateDTO, Long userId, Long currentUserId){
        return userAccountAvailableToInteraction(currentUserId, userId)
                .flatMap(user -> {
                    if (userUpdateDTO.getFirstName() != null) {
                        user.setFirstName(userUpdateDTO.getFirstName());
                    }
                    if (userUpdateDTO.getLastName() != null) {
                        user.setLastName(userUpdateDTO.getLastName());
                    }
                    if(userUpdateDTO.getPassword() != null){
                        user.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
                    }

                    return userRepository.save(user)
                            .map(userMapper::toDto)
                            .doOnSuccess(success -> log.info("User updated successfully"))
                            .onErrorResume(ex -> {
                                log.error("Error updating user data: {}", ex.getMessage());
                                return Mono.error(new DatabaseOperationException("User updated failed"));
                            });
                });
    }


    public Mono<Void> deleteUser(Long userId, Long currentUserId){
       return userAccountAvailableToInteraction(currentUserId, userId)
               .flatMap(userRepository::delete)
               .doOnSuccess(success -> log.info("User {} successfully deleted", userId))
               .onErrorResume(ex -> {
                   log.error("Failed to delete user {}: {}", userId, ex.getMessage());
                   return Mono.error(new DatabaseOperationException("Failed to delete user"));
               });
    }


    private Mono<User> userAccountAvailableToInteraction(Long currentUserId, Long userId){
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotExistException("User does not exist")))
                .filter(user -> user.getUserId().equals(currentUserId))
                .switchIfEmpty(Mono.error(new UserNotAvailableException("User is not available")));
    }
}


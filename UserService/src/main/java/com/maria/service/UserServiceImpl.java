package com.maria.service;

import com.maria.constant.UserServiceConstants;
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
                .switchIfEmpty(Mono.error(new UserNotExistException(UserServiceConstants.NOT_EXIST)))
                .map(userMapper::toDto)
                .onErrorMap(ex -> {
                    log.warn(UserServiceConstants.LOG_FAILED_GET_USER, ex.getMessage());
                    if (ex instanceof UserNotExistException) {
                        return ex;
                    }
                    return new DatabaseOperationException(UserServiceConstants.DATABASE_ERROR);
                });
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotExistException(UserServiceConstants.NOT_EXIST)))
                .onErrorMap(ex -> {
                    log.warn(UserServiceConstants.LOG_FAILED_GET_USER, ex.getMessage());
                    if (ex instanceof UserNotExistException) {
                        return ex;
                    }
                    return new DatabaseOperationException(UserServiceConstants.DATABASE_ERROR);
                });
    }

    @Override
    public Mono<UserDTO> registerUser(UserRegistrationDTO userRegistrationDTO) {
        User newUser = User.builder()
                .firstName(userRegistrationDTO.getFirstName())
                .lastName(userRegistrationDTO.getLastName())
                .email(userRegistrationDTO.getEmail())
                .password(passwordEncoder.encode(userRegistrationDTO.getPassword()))
                .role("user")
                .build();

        return userRepository.findByEmail(userRegistrationDTO.getEmail())
                .flatMap(existingUser -> Mono.error(new UserAlreadyExistsException(UserServiceConstants.EMAIL_EXISTS)))
                .then(Mono.defer(() -> userRepository.save(newUser)))
                .map(userMapper::toDto)
                .doOnSuccess(success -> log.info(UserServiceConstants.SUCCESSFULLY_REGISTERED))
                .onErrorMap(ex -> {
                    log.error(UserServiceConstants.LOG_REGISTR_ERROR, ex.getMessage());
                    if (ex instanceof UserAlreadyExistsException) {
                        return ex;
                    }
                    return new DatabaseOperationException(UserServiceConstants.DATABASE_ERROR);
                });
    }

    @Override
    public Mono<UserDTO> updateUser(UserUpdateDTO userUpdateDTO, Long userId, Long currentUserId) {
        return userAccountAvailableToInteraction(currentUserId, userId)
                .flatMap(user -> {
                    if (userUpdateDTO.getFirstName() != null) {
                        user.setFirstName(userUpdateDTO.getFirstName());
                    }
                    if (userUpdateDTO.getLastName() != null) {
                        user.setLastName(userUpdateDTO.getLastName());
                    }
                    if (userUpdateDTO.getPassword() != null) {
                        user.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
                    }

                    return userRepository.save(user)
                            .map(userMapper::toDto)
                            .doOnSuccess(success -> log.info(UserServiceConstants.LOG_UPDATE_SUCCESS, userId))
                            .onErrorResume(ex -> {
                                log.error(UserServiceConstants.LOG_UPDATE_ERROR, ex.getMessage());
                                return Mono.error(new DatabaseOperationException(UserServiceConstants.DATABASE_ERROR));
                            });
                });
    }

    public Mono<Void> deleteUser(Long userId, Long currentUserId) {
        return userAccountAvailableToInteraction(currentUserId, userId)
                .flatMap(userRepository::delete)
                .doOnSuccess(success -> log.info(UserServiceConstants.LOG_DELETE_SUCCESS, userId))
                .onErrorMap(ex -> {
                    log.error(UserServiceConstants.LOG_DELETE_ERROR, ex.getMessage());
                    if (ex instanceof UserNotExistException || ex instanceof UserNotAvailableException) {
                        return ex;
                    }
                    return new DatabaseOperationException(UserServiceConstants.DATABASE_ERROR);
                });
    }

    private Mono<User> userAccountAvailableToInteraction(Long currentUserId, Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotExistException(UserServiceConstants.NOT_EXIST)))
                .filter(user -> user.getUserId().equals(currentUserId))
                .switchIfEmpty(Mono.error(new UserNotAvailableException(UserServiceConstants.NOT_AVAILABLE)));
    }
}


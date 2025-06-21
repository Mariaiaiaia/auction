package com.maria.service;

import com.maria.constant.UserServiceConstants;
import com.maria.dto.UserDTO;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.entity.User;
import com.maria.exception.DatabaseOperationException;
import com.maria.exception.UserAlreadyExistsException;
import com.maria.exception.UserNotExistException;
import com.maria.mapper.UserMapper;
import com.maria.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserInfo_UserExists_ReturnsUserDTO() {
        Long userId = 1L;
        User user = new User(userId, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");
        UserDTO userDTO = new UserDTO(userId, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com");

        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        Mockito.when(userMapper.toDto(user)).thenReturn(userDTO);

        Mono<UserDTO> result = userService.getUserInfo(userId);

        StepVerifier
                .create(result)
                .expectNext(userDTO)
                .expectComplete()
                .verify();

        Mockito.verify(userRepository).findById(userId);
        Mockito.verifyNoMoreInteractions(userMapper);
    }

    @Test
    void getUserInfo_UserNotExists_ThrowsUserNotExistsException() {
        Long userId = 1L;

        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.empty());

        Mono<UserDTO> result = userService.getUserInfo(userId);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof UserNotExistException &&
                        throwable.getMessage().equals(UserServiceConstants.NOT_EXIST))
                .verify();

        Mockito.verify(userRepository).findById(userId);
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void getUserInfo_RepositoryThrowsException() {
        Long userId = 1L;

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Mono.error(new DatabaseOperationException(UserServiceConstants.DATABASE_ERROR)));

        Mono<UserDTO> result = userService.getUserInfo(userId);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof DatabaseOperationException &&
                        throwable.getMessage().equals(UserServiceConstants.DATABASE_ERROR))
                .verify();

        Mockito.verify(userRepository).findById(userId);
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void getUserByEmail_UserExists_ReturnsUserDTO() {
        User user = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");

        Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));

        Mono<User> result = userService.getUserByEmail(user.getEmail());

        StepVerifier
                .create(result)
                .expectNext(user)
                .expectComplete()
                .verify();

        Mockito.verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void getUserByEmail_UserNotExists_ThrowsUserNotExistsException() {
        String userEmail = "harryp@gmail.com";

        Mockito.when(userRepository.findByEmail(userEmail)).thenReturn(Mono.empty());

        Mono<User> result = userService.getUserByEmail(userEmail);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof UserNotExistException &&
                        throwable.getMessage().equals(UserServiceConstants.NOT_EXIST))
                .verify();

        Mockito.verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void getUserByEmail_ThrowsDatabaseException() {
        String userEmail = "harryp@gmail.com";

        Mockito.when(userRepository.findByEmail(userEmail))
                .thenReturn(Mono.error(new DatabaseOperationException(UserServiceConstants.DATABASE_ERROR)));

        Mono<User> result = userService.getUserByEmail(userEmail);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof DatabaseOperationException &&
                        throwable.getMessage().equals(UserServiceConstants.DATABASE_ERROR))
                .verify();

        Mockito.verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void registerUser_UserRegistered_ReturnsUserDTO() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO("Harry", "Potter",
                "expelliarmus", "harryp@gmail.com");
        User user = new User(null, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");
        UserDTO userDTO = new UserDTO(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com");

        Mockito.when(userRepository.findByEmail(userRegistrationDTO.getEmail())).thenReturn(Mono.empty());
        Mockito.when(passwordEncoder.encode(userRegistrationDTO.getPassword())).thenReturn("expelliarmus");
        Mockito.when(userRepository.save(user)).thenReturn(Mono.just(user));
        Mockito.when(userMapper.toDto(user)).thenReturn(userDTO);

        Mono<UserDTO> result = userService.registerUser(userRegistrationDTO);

        StepVerifier
                .create(result)
                .expectNext(userDTO)
                .expectComplete()
                .verify();

        Mockito.verify(userRepository).findByEmail(userRegistrationDTO.getEmail());
        Mockito.verifyNoMoreInteractions(userMapper);
    }

    @Test
    void registerUser_ThrowsUserAlreadyExistsException() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO("Harry", "Potter",
                "expelliarmus", "harryp@gmail.com");
        User user = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");

        Mockito.when(userRepository.findByEmail(userRegistrationDTO.getEmail())).thenReturn(Mono.just(user));

        Mono<UserDTO> result = userService.registerUser(userRegistrationDTO);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof UserAlreadyExistsException &&
                        throwable.getMessage().equals(UserServiceConstants.EMAIL_EXISTS))
                .verify();

        Mockito.verify(userRepository).findByEmail(userRegistrationDTO.getEmail());
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void registerUser_ThrowsDatabaseOperationException() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO("Harry", "Potter",
                "expelliarmus", "harryp@gmail.com");
        User user = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");

        Mockito.when(userRepository.findByEmail(userRegistrationDTO.getEmail())).thenReturn(Mono.empty());
        Mockito.when(userRepository.save(user))
                .thenReturn(Mono.error(new RuntimeException("Database failure")));

        Mono<UserDTO> result = userService.registerUser(userRegistrationDTO);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof DatabaseOperationException &&
                        throwable.getMessage().equals(UserServiceConstants.DATABASE_ERROR))
                .verify();

        Mockito.verify(userRepository).findByEmail(userRegistrationDTO.getEmail());
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void updateUser_UserUpdated_ReturnsUserDTO() {
        Long currentUserId = 1L;
        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .firstName("Ron")
                .lastName("Weasley")
                .password("newPass")
                .build();
        User existingUser = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");
        User updatedUser = new User(1L, "Ron", "Weasley", "newPass",
                "harryp@gmail.com", "user");
        UserDTO userDTO = new UserDTO(1L, "Ron", "Weasley", "newPass",
                "harryp@gmail.com");

        Mockito.when(userRepository.findById(existingUser.getUserId())).thenReturn(Mono.just(existingUser));
        Mockito.when(passwordEncoder.encode(userUpdateDTO.getPassword())).thenReturn("newPass");
        Mockito.when(userRepository.save(updatedUser)).thenReturn(Mono.just(updatedUser));
        Mockito.when(userMapper.toDto(updatedUser)).thenReturn(userDTO);

        Mono<UserDTO> result = userService.updateUser(userUpdateDTO, existingUser.getUserId(), currentUserId);

        StepVerifier
                .create(result)
                .expectNext(userDTO)
                .expectComplete()
                .verify();

        Mockito.verifyNoMoreInteractions(userMapper);
        Mockito.verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void updateUser_NoUpdates_ReturnsUserDTO() {
        Long currentUserId = 1L;
        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .build();
        User existingUser = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");
        UserDTO userDTO = new UserDTO(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com");

        Mockito.when(userRepository.findById(existingUser.getUserId())).thenReturn(Mono.just(existingUser));
        Mockito.when(userRepository.save(existingUser)).thenReturn(Mono.just(existingUser));
        Mockito.when(userMapper.toDto(existingUser)).thenReturn(userDTO);

        Mono<UserDTO> result = userService.updateUser(userUpdateDTO, existingUser.getUserId(), currentUserId);

        StepVerifier
                .create(result)
                .expectNext(userDTO)
                .expectComplete()
                .verify();

        Mockito.verifyNoMoreInteractions(userMapper);
        Mockito.verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateUser_throwsDatabaseOperationException() {
        Long currentUserId = 1L;
        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .lastName("Weasley")
                .password("newPass")
                .build();
        User existingUser = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");
        User updatedUser = new User(1L, "Harry", "Weasley", "newPass",
                "harryp@gmail.com", "user");

        Mockito.when(userRepository.findById(existingUser.getUserId())).thenReturn(Mono.just(existingUser));
        Mockito.when(passwordEncoder.encode(userUpdateDTO.getPassword())).thenReturn("newPass");
        Mockito.when(userRepository.save(updatedUser)).thenReturn(Mono.error(new DatabaseOperationException(UserServiceConstants.UPDATED_FAILED)));

        Mono<UserDTO> result = userService.updateUser(userUpdateDTO, existingUser.getUserId(), currentUserId);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof DatabaseOperationException
                        && throwable.getMessage().equals(UserServiceConstants.DATABASE_ERROR))
                .verify();

        Mockito.verifyNoInteractions(userMapper);
        Mockito.verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void deleteUser_SuccessfulDeletion() {
        Long currentUserId = 1L;
        User existingUser = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");

        Mockito.when(userRepository.findById(existingUser.getUserId())).thenReturn(Mono.just(existingUser));
        Mockito.when(userRepository.delete(existingUser)).thenReturn(Mono.empty());

        Mono<Void> result = userService.deleteUser(existingUser.getUserId(), currentUserId);

        StepVerifier
                .create(result)
                .verifyComplete();

        Mockito.verifyNoMoreInteractions(userMapper);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_throwsDatabaseOperationException() {
        Long currentUserId = 1L;
        User existingUser = new User(1L, "Harry", "Potter", "expelliarmus",
                "harryp@gmail.com", "user");

        Mockito.when(userRepository.findById(existingUser.getUserId())).thenReturn(Mono.just(existingUser));
        Mockito.when(userRepository.delete(existingUser)).thenReturn(Mono.error(new DatabaseOperationException(UserServiceConstants.FAILED_DELETE)));

        Mono<Void> result = userService.deleteUser(existingUser.getUserId(), currentUserId);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof DatabaseOperationException &&
                        throwable.getMessage().equals(UserServiceConstants.DATABASE_ERROR))
                .verify();

        Mockito.verifyNoMoreInteractions(userRepository);
    }
}

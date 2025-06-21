package com.maria.mapper;

import com.maria.dto.UserDTO;
import com.maria.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);

    User toEntity(UserDTO userDTO);
}

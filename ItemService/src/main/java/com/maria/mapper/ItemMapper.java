package com.maria.mapper;

import com.maria.dto.ItemDTO;
import com.maria.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDTO toDto(Item item);

    Item toEntity(ItemDTO itemDTO);
}



package com.maria.mapper;

import com.maria.dto.BidDTO;
import com.maria.entity.Bid;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BidMapper {
    Bid toEntity(BidDTO bidDTO);

    BidDTO toDto(Bid bid);
}

package com.maria.mapper;

import com.maria.core.entity.AuctionDTO;
import com.maria.entity.Auction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuctionMapper {
    Auction toEntity(AuctionDTO auctionDTO);
    AuctionDTO toDto(Auction auction);

}

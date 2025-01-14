package com.maria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuctionRequestDTO {
    private Long itemId;
    private BigDecimal startingPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isPublic;
}

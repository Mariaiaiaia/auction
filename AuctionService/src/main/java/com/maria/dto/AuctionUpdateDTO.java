package com.maria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuctionUpdateDTO {
    private BigDecimal startingPrice;
    private Boolean publicAccess;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

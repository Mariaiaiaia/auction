package com.maria.dto;

import com.maria.constant.AuctionServiceConstants;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuctionUpdateDTO {
    @DecimalMin(value = "0.0", inclusive = false, message = AuctionServiceConstants.VALIDATION_STARTING_PRICE)
    private BigDecimal startingPrice;
    private Boolean publicAccess;
    @Future(message = AuctionServiceConstants.VALIDATION_START_DATE)
    private LocalDateTime startDate;
    @Future(message = AuctionServiceConstants.VALIDATION_END_DATE)
    private LocalDateTime endDate;
}

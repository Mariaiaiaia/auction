package com.maria.dto;

import com.maria.constant.BidServiceConstants;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBidRequest {
    @NotNull(message = BidServiceConstants.VALIDATION_BID_VALUE_REQ)
    @DecimalMin(value = "0.0", inclusive = false, message = BidServiceConstants.VALIDATION_BID_VALUE)
    private BigDecimal bidAmount;
    @NotNull(message = BidServiceConstants.VALIDATION_AUCTION_ID_REQ)
    private Long auctionId;
}

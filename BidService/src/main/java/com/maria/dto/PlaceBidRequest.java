package com.maria.dto;

import com.maria.constant.BidServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for placing a new bid on an auction")
public class PlaceBidRequest {
    @Schema(description = "Bid amount offered by the user. Must be greater than zero.", example = "150.00")
    @NotNull(message = BidServiceConstants.VALIDATION_BID_VALUE_REQ)
    @DecimalMin(value = "0.0", inclusive = false, message = BidServiceConstants.VALIDATION_BID_VALUE)
    private BigDecimal bidAmount;
    @Schema(description = "ID of the auction the bid is placed on", example = "11")
    @NotNull(message = BidServiceConstants.VALIDATION_AUCTION_ID_REQ)
    private Long auctionId;
}

package com.maria.dto;

import com.maria.constant.AuctionServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO for updating an auction")
public class AuctionUpdateDTO {
    @Schema(description = "To update the auction start price", example = "50.00", minimum = "0.0")
    @DecimalMin(value = "0.0", inclusive = false, message = AuctionServiceConstants.VALIDATION_STARTING_PRICE)
    private BigDecimal startingPrice;
    @Schema(description = "To allow or disallow public access to the auction", example = "true")
    private Boolean publicAccess;
    @Schema(description = "To update start date and time of the auction (must be in the future)", example = "2025-12-02T10:00:00.000000")
    @Future(message = AuctionServiceConstants.VALIDATION_START_DATE)
    private LocalDateTime startDate;
    @Schema(description = "To update end date and time of the auction (must be in the future)", example = "2025-12-03T10:00:00.000000")
    @Future(message = AuctionServiceConstants.VALIDATION_END_DATE)
    private LocalDateTime endDate;
}

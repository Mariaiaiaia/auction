package com.maria.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maria.constant.AuctionServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for creating an auction")
public class CreateAuctionRequestDTO {
    @Schema(description = "ID of the item to be auctioned", example = "101")
    @NotNull(message = AuctionServiceConstants.VALIDATION_ITEM_REQ)
    private Long itemId;
    @Schema(description = "Starting price of the auction", example = "50.00", minimum = "0.0")
    @NotNull(message = AuctionServiceConstants.VALIDATION_PRICE_REQ)
    @DecimalMin(value = "0.0", inclusive = false, message = AuctionServiceConstants.VALIDATION_STARTING_PRICE)
    private BigDecimal startingPrice;
    @Schema(description = "Start date and time of the auction (must be in the future)", example = "2025-12-01T10:00:00.000000")
    @NotNull(message = AuctionServiceConstants.VALIDATION_START_DATE_REQ)
    @Future(message = AuctionServiceConstants.VALIDATION_START_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime startDate;
    @Schema(description = "End date and time of the auction (must be in the future)", example = "2025-12-05T10:00:00.000000")
    @NotNull(message = AuctionServiceConstants.VALIDATION_END_DATE_REQ)
    @Future(message = AuctionServiceConstants.VALIDATION_END_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime endDate;
    @Schema(description = "Whether the auction is public or private", example = "true")
    @JsonProperty("public")
    private boolean isPublic;
}

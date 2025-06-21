package com.maria.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maria.constant.AuctionServiceConstants;
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
public class CreateAuctionRequestDTO {
    @NotNull(message = AuctionServiceConstants.VALIDATION_ITEM_REQ)
    private Long itemId;
    @NotNull(message = AuctionServiceConstants.VALIDATION_PRICE_REQ)
    @DecimalMin(value = "0.0", inclusive = false, message = AuctionServiceConstants.VALIDATION_STARTING_PRICE)
    private BigDecimal startingPrice;
    @NotNull(message = AuctionServiceConstants.VALIDATION_START_DATE_REQ)
    @Future(message = AuctionServiceConstants.VALIDATION_START_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime startDate;
    @NotNull(message = AuctionServiceConstants.VALIDATION_END_DATE_REQ)
    @Future(message = AuctionServiceConstants.VALIDATION_END_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime endDate;
    @JsonProperty("public")
    private boolean isPublic;
}

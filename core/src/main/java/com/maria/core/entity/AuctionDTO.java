package com.maria.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing an auction")
public class AuctionDTO {
    @Schema(description = "Unique ID of the auction", example = "101")
    private Long auctionId;
    @Schema(description = "ID of the item being auctioned", example = "5001")
    private Long itemId;
    @Schema(description = "Starting price of the auction", example = "100.00")
    private BigDecimal startingPrice;
    @Schema(description = "Current highest bid price", example = "250.00")
    private BigDecimal currentPrice;
    @Schema(description = "ID of the seller (user who created the auction)", example = "42")
    private Long sellerId;
    @Schema(description = "Whether the auction is public", example = "true")
    private boolean publicAccess;
    @Schema(description = "Start date and time of the auction", example = "2025-08-10T12:00:00")
    private LocalDateTime startDate;
    @Schema(description = "End date and time of the auction", example = "2025-08-12T12:00:00")
    private LocalDateTime endDate;
}
package com.maria.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("bid")
@Schema(description = "Model representing a user's bid on an auction")
public class Bid {
    @Schema(description = "Unique identifier of the bid", example = "101")
    @Id
    @Column("id")
    private Long bidId;
    @Schema(description = "ID of the user who placed the bid", example = "7")
    @Column("user_id")
    private Long userId;
    @Schema(description = "ID of the auction the bid belongs to", example = "23")
    @Column("auction_id")
    private Long auctionId;
    @Schema(description = "Amount of the bid", example = "150.00", minimum = "0.01")
    @Column("bid_amount")
    private BigDecimal bidAmount;
}

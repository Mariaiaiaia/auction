package com.maria.entity;

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
public class Bid {
    @Id
    @Column("id")
    private Long bidId;
    @Column("user_id")
    private Long userId;
    @Column("auction_id")
    private Long auctionId;
    @Column("bid_amount")
    private BigDecimal bidAmount;
}

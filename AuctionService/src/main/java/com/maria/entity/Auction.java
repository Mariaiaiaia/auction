package com.maria.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("auction")
public class Auction {
    @Id
    @Column("id")
    private Long auctionId;
    @Column("item")
    private Long itemId;
    @Column("seller")
    private Long sellerId;
    @Column("starting_price")
    private BigDecimal startingPrice;
    @Column("current_price")
    private BigDecimal currentPrice;
    @Column("bidder")
    private Long bidderId;
    @Column("start_date")
    private LocalDateTime startDate;
    @Column("end_date")
    private LocalDateTime endDate;
    @Column("finished")
    private boolean finished;
    @Column("public_access")
    private Boolean publicAccess;
}

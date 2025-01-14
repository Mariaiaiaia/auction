package com.maria.entity;

import com.maria.dto.AuctionUpdateDTO;
import lombok.Builder;
import lombok.Data;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@Data
@Table("auction")
public class Auction {
    @Id
    @Column("id")
    private Long auctionId;
    @Column("item")
    private final Long itemId;
    @Column("seller")
    private final Long sellerId;
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

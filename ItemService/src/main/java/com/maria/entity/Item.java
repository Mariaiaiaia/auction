package com.maria.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("item")
public class Item {
    @Id
    @Column("id")
    private Long itemId;
    private String itemName;
    private String description;
    private String image;
    private boolean isSold;
    private Long auctionId;
    @Column("seller")
    private Long sellerId;
}

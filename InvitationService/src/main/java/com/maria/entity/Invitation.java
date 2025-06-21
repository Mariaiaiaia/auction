package com.maria.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table("invitation")
public class Invitation {
    @Id
    @Column("id")
    private Long invitationId;
    @Column("auction")
    private Long auctionId;
    @Column("seller")
    private Long sellerId;
    @Column("usr")
    private Long userId;
    private Boolean acceptance;
}

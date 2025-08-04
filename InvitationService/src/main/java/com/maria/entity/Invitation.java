package com.maria.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entity representing an invitation to a private auction")
public class Invitation {
    @Schema(description = "Unique identifier of the invitation", example = "1001")
    @Id
    @Column("id")
    private Long invitationId;
    @Schema(description = "ID of the auction the user is invited to", example = "501")
    @Column("auction")
    private Long auctionId;
    @Schema(description = "ID of the seller who created the invitation", example = "301")
    @Column("seller")
    private Long sellerId;
    @Schema(description = "ID of the invited user", example = "401")
    @Column("usr")
    private Long userId;
    @Schema(description = "Whether the user accepted the invitation", example = "true")
    private Boolean acceptance;
}

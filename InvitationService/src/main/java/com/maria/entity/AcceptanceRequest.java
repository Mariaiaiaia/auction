package com.maria.entity;

import com.maria.constant.InvitationServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for responding to an invitation")
public class AcceptanceRequest {
    @Schema(description = "ID of the auction to which the invitation belongs", example = "101")
    @NotNull(message = InvitationServiceConstants.VALID_AUCTION_ID_REQ)
    private Long auctionId;
    @Schema(description = "User's response: true for acceptance, false for rejection", example = "true")
    @NotNull(message = InvitationServiceConstants.VALID_RESPONSE_REQ)
    private boolean acceptance;
}

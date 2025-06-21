package com.maria.entity;

import com.maria.constant.InvitationServiceConstants;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptanceRequest {
    @NotNull(message = InvitationServiceConstants.VALID_AUCTION_ID_REQ)
    private Long auctionId;
    @NotNull(message = InvitationServiceConstants.VALID_RESPONSE_REQ)
    private boolean acceptance;
}

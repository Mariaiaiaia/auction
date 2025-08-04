package com.maria.entity;

import com.maria.constant.ItemServiceConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO for creating an item")
public class CreateItemRequest {
    @Schema(description = "Name of the item", example = "Invisibility Cloak")
    @NotNull(message = ItemServiceConstants.EX_ITEM_NAME_REQ)
    private String itemName;
    @Schema(description = "Description of the item", example = "A magical cloak that renders the wearer invisible.")
    @NotNull(message = ItemServiceConstants.EX_ITEM_DESCRIPTION_REQ)
    private String description;
}

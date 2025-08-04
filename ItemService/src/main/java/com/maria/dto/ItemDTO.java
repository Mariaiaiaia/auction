package com.maria.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(description = "Data Transfer Object representing an item")
public class ItemDTO {
    @Schema(description = "Name of the item", example = "Harry Potter's Wand")
    private String itemName;
    @Schema(description = "Detailed description of the item", example = "An original holly and phoenix feather wand used by Harry Potter.")
    private String description;
    @Schema(description = "URL to the image of the item", example = "https://auction-item.s3.eu-north-1.amazonaws.com/wand.jpg")
    private String image;
    @Schema(description = "ID of the seller who listed the item", example = "777")
    private Long sellerId;
}

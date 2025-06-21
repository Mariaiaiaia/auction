package com.maria.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ItemDTO {
    private String itemName;
    private String description;
    private String image;
    private Long sellerId;
}

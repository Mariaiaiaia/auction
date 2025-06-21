package com.maria.core.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ItemDTO {
    private String itemName;
    private String description;
    private String image;
    private Long sellerId;
}

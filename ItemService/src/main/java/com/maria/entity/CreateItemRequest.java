package com.maria.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateItemRequest {
    private String itemName;
    private String description;
}

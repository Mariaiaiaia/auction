package com.maria.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
public class Notification {
    @Id
    private String id;
    private Long userId;
    private Long auctionId;
    private String message;
    private String title;
    private LocalDateTime timestamp;
    private boolean isRead;
    private Long itemId;
}


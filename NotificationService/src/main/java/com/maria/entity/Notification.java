package com.maria.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@Schema(description = "Model representing a notification")
public class Notification {
    @Schema(description = "Notification ID", example = "64fae2cd12345b7890abcd12")
    @Id
    private String id;
    @Schema(description = "ID of the user receiving the notification", example = "1001")
    private Long userId;
    @Schema(description = "ID of the related auction", example = "2005")
    private Long auctionId;
    @Schema(description = "Text of the notification message", example = "The auction for your item has ended.")
    private String message;
    @Schema(description = "Short title of the notification", example = "Auction Ended")
    private String title;
    @Schema(description = "Date and time when the notification was created", example = "2025-08-02T14:30:00")
    private LocalDateTime timestamp;
    @Schema(description = "Indicates whether the notification has been read", example = "false")
    private boolean isRead;
    @Schema(description = "ID of the item related to the notification", example = "305")
    private Long itemId;
}


package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VinMessage {
    private Long id;
    private Long vinRequestId;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;
}
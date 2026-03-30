package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Return {
    private Long id;
    private Long orderItemId;
    private String reason;
    private String status; // REQUESTED, APPROVED, REJECTED, COMPLETED
    private LocalDateTime createdAt;
}
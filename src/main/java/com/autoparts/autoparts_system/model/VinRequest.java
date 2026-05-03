package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VinRequest {
    private Long id;
    private Long userId;
    private String vin;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
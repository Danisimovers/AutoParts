package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRequest {
    private Long id;
    private Long userId;
    private String productName;
    private String factoryNumber;
    private String producer;
    private String supplierName;
    private Double price;
    private String status;
    private LocalDateTime createdAt;
}
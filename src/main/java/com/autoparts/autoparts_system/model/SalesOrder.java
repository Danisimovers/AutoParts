package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {
    private Long id;
    private Long userId;
    private BigDecimal total;
    private String status; // CREATED, PAID, SHIPPED, DELIVERED, CANCELLED
    private LocalDateTime createdAt;
}
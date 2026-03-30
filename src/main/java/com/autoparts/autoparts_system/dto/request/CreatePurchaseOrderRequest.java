package com.autoparts.autoparts_system.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {
    private Long supplierId;
    private Map<Long, Integer> items; // productId -> quantity
    private BigDecimal total;
}
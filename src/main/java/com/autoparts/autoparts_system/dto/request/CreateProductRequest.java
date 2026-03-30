package com.autoparts.autoparts_system.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private Long manufacturerId;
    private String oemCode;
    private List<Long> compatibleVehicleIds; // ID авто для совместимости
}
package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
    public class ProductDetailDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryName;
    private String manufacturerName;
    private String oemCode;
    private Integer stock;
    private List<String> compatibleVehicles;
}
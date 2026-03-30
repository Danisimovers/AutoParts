package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private String categoryName;
    private String manufacturerName;
    private Integer stock;
}
package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private String itemId;
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal total;
}
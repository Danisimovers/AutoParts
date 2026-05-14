package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long id;
    private Long cartId;
    private String tempId;          // ключ для фронтенда (PROD_123, EXT_ABC)
    private Long productId;         // для REGULAR
    private String itemType;        // REGULAR или EXTERNAL
    private String productName;     // для EXTERNAL
    private String factoryNumber;   // для EXTERNAL
    private String producer;        // для EXTERNAL
    private String supplierName;    // для EXTERNAL
    private BigDecimal price;
    private Integer quantity;
}
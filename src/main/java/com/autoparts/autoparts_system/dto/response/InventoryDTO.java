package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private String warehouseId;
}
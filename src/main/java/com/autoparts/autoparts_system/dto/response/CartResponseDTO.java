package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private List<CartItemDTO> items;
    private int totalItems;
    private BigDecimal totalPrice;
}
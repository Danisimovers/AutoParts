package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVehicleCompat {
    private Long id;
    private Long productId;
    private Long vehicleId;
    private String details; // доп. информация по совместимости
}
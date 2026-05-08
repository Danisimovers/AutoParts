package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProduct {
    private Long id;
    private String factoryNumber;
    private String name;
    private String producer;
    private Double price;
    private Integer stock;
    private String delivery;
    private String supplierName;
    private Long supplierId;
}
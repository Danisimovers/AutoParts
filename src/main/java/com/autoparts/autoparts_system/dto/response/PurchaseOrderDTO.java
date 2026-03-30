package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDate date;
    private String status;
    private BigDecimal total;
}
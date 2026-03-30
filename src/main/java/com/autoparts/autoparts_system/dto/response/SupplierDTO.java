package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private String phone;
    private String address;
}
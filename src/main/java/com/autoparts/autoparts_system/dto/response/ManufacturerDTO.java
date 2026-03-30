package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerDTO {
    private Long id;
    private String name;
    private String country;
    private String contactInfo;
}
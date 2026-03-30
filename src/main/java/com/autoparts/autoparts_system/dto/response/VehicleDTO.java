package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Long id;
    private String make;
    private String model;
    private String generation;
    private Integer yearFrom;
    private Integer yearTo;
    private String engine;
}
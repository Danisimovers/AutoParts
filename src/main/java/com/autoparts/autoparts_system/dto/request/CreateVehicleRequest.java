package com.autoparts.autoparts_system.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleRequest {
    private String make;
    private String model;
    private String generation;
    private Integer yearFrom;
    private Integer yearTo;
    private String engine;
}
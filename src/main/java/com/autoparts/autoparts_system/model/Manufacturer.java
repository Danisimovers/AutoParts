package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer {
    private Long id;
    private String name;
    private String country;
    private String contactInfo;
}
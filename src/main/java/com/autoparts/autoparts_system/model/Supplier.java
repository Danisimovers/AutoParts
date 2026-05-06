package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private String phone;
    private String address;
    private String apiUrl;
    private String apiKey;
}
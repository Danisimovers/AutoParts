package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private String query;
    private Long vehicleId;
    private String vehicleName;
    private int totalResults;
    private List<ProductDTO> products;
}
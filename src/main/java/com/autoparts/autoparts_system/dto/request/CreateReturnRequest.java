package com.autoparts.autoparts_system.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {
    private Long orderItemId;
    private String reason;
}
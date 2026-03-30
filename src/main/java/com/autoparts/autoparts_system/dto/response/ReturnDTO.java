package com.autoparts.autoparts_system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDTO {
    private Long id;
    private Long orderItemId;
    private Long orderId;
    private String productName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
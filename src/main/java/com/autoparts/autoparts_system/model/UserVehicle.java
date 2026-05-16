package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVehicle {
    private Long id;
    private Long userId;
    private Long vehicleId;
    private String vin;
    private String licensePlate;
    private String nickname;
    private Integer year;
    private LocalDateTime createdAt;

    // Дополнительные поля для отображения (из vehicles)
    private String make;
    private String model;
    private String generation;
}
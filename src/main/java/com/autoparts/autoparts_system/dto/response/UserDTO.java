package com.autoparts.autoparts_system.dto.response;

import com.autoparts.autoparts_system.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String login;
    private String email;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;
}
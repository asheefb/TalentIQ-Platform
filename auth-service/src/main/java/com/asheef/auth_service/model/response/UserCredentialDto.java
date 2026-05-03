package com.asheef.auth_service.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight projection of a user exposed by user-service's internal endpoint
 * for authentication purposes. Owned locally to avoid cross-module coupling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialDto {
    private Integer id;
    private String email;
    private String password;   // BCrypt hash
    private String role;
    private Boolean isActive;
}

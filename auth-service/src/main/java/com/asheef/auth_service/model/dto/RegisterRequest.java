package com.asheef.auth_service.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for POST /auth/register. The auth-service owns user creation from the
 * client's perspective and delegates persistence to user-service via WebClient.
 */
@Data
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name should not be blank")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]d{9}$", message = "Invalid mobile format")
    private String mobile;

    private String address;

    @NotBlank(message = "Password should not be empty")
    @Pattern(regexp = "^.{8,}$", message = "Password should be minimum 8 characters long")
    private String password;
}

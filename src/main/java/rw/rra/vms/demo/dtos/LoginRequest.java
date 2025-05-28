package rw.rra.vms.demo.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for user login requests, with validation for email and password.
 */
@Data
public class LoginRequest {
    /**
     * Email, must be valid.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Password, must not be empty.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
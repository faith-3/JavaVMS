package rw.rra.vms.demo.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for user signup requests, with validation for all fields.
 */
@Data
public class SignupRequest {
    /**
     * Name of the user, must not be empty.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Email, must be valid and unique.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Phone number, must be 10 digits (Rwanda format).
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;

    /**
     * National ID, must be 16 digits (adjust based on Rwanda's format).
     */
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "\\d{16}", message = "National ID must be 16 digits")
    private String nationalId;

    /**
     * Password, must be at least 8 characters.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Role, must be either ROLE_ADMIN or ROLE_STANDARD.
     */
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "(ADMIN|STANDARD)", message = "Role must be ADMIN or STANDARD")
    private String role;
}

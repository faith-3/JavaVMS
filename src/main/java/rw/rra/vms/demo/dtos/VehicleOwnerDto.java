package rw.rra.vms.demo.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for vehicle owner data with validation.
 */
@Data
public class VehicleOwnerDto {
    /**
     * Owner's full name.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Owner's national ID, must be 16 digits.
     */
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "\\d{16}", message = "National ID must be 16 digits")
    private String nationalId;

    /**
     * Owner's phone number, must be 10 digits.
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;

    /**
     * Owner's address.
     */
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    /**
     * Owner's email, must be valid.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
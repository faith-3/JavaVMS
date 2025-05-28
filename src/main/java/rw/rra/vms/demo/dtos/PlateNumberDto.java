package rw.rra.vms.demo.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PlateNumberDto {
    @NotBlank(message = "Plate number is required")
    @Pattern(regexp = "[A-Z0-9]{5,10}", message = "Plate number must be 5-10 alphanumeric characters")
    private String plateNumber;

    @NotNull(message = "Issued date is required")
    @PastOrPresent(message = "Issued date must be today or in the past")
    private LocalDate issuedDate;

    private boolean inUse;
}

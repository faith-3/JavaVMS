package rw.rra.vms.demo.Controllers;

import jakarta.validation.Valid;
import rw.rra.vms.demo.dtos.PlateNumberDto;
import rw.rra.vms.demo.dtos.VehicleOwnerDto;
import rw.rra.vms.demo.Entities.PlateNumber;
import rw.rra.vms.demo.Entities.VehicleOwner;
import rw.rra.vms.demo.Repositories.PlateNumberRepository;
import rw.rra.vms.demo.Repositories.VehicleOwnerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/owners")
@Tag(name = "Owner Controller", description = "Vehicle owner management")
@RequiredArgsConstructor
public class OwnerController {

    private static final Logger logger = LoggerFactory.getLogger(OwnerController.class);

    private final VehicleOwnerRepository ownerRepository;
    private final PlateNumberRepository plateNumberRepository;

    private VehicleOwnerDto toDto(VehicleOwner owner) {
        VehicleOwnerDto dto = new VehicleOwnerDto();
        dto.setName(owner.getName());
        dto.setNationalId(owner.getNationalId());
        dto.setPhone(owner.getPhone());
        dto.setAddress(owner.getAddress());
        dto.setEmail(owner.getEmail());
        return dto;
    }

    private VehicleOwner toEntity(VehicleOwnerDto dto) {
        VehicleOwner owner = new VehicleOwner();
        owner.setName(dto.getName());
        owner.setNationalId(dto.getNationalId());
        owner.setPhone(dto.getPhone());
        owner.setAddress(dto.getAddress());
        owner.setEmail(dto.getEmail());
        return owner;
    }

    @Operation(summary = "Register a new vehicle owner")
    @PostMapping
    public ResponseEntity<?> registerOwner(@Valid @RequestBody VehicleOwnerDto ownerDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.error("Validation error in {}: {}", error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        if (ownerRepository.findByNationalId(ownerDto.getNationalId()).isPresent()) {
            logger.warn("Registration attempt with duplicate national ID: {}", ownerDto.getNationalId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("National ID already exists");
        }

        if (ownerRepository.findByEmail(ownerDto.getEmail()).isPresent()) {
            logger.warn("Registration attempt with duplicate email: {}", ownerDto.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        VehicleOwner saved = ownerRepository.save(toEntity(ownerDto));
        logger.info("Owner registered successfully: {}", ownerDto.getEmail());
        return ResponseEntity.ok(toDto(saved));
    }

    @Operation(summary = "Get paginated list of vehicle owners")
    @GetMapping
    public ResponseEntity<?> getOwners(@RequestParam @Min(value = 0, message = "Page must be non-negative") int page,
                                       @RequestParam @Min(value = 1, message = "Size must be at least 1") int size) {
        return ResponseEntity.ok(ownerRepository.findAll(PageRequest.of(page, size))
                .map(this::toDto));
    }

    @Operation(summary = "Search owner by national ID or phone")
    @GetMapping("/search")
    public ResponseEntity<?> searchOwner(@RequestParam(required = false) String nationalId,
                                         @RequestParam(required = false) String phone,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.error("Validation error in {}: {}", error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        if (nationalId == null && phone == null) {
            logger.warn("Search attempt with no parameters");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least one search parameter (nationalId or phone) is required");
        }

        VehicleOwner owner = null;
        if (nationalId != null) owner = ownerRepository.findByNationalId(nationalId).orElse(null);
        else if (phone != null) owner = ownerRepository.findByPhone(phone).orElse(null);

        if (owner == null) {
            logger.warn("Owner not found for nationalId: {} or phone: {}", nationalId, phone);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Owner not found");
        }

        logger.info("Owner found: {}", owner.getEmail());
        return ResponseEntity.ok(toDto(owner));
    }

    @Operation(summary = "Add a plate number to an owner")
    @PostMapping("/{ownerId}/plate")
    public ResponseEntity<?> registerPlate(@PathVariable @Min(value = 1, message = "Owner ID must be positive") Long ownerId,
                                           @Valid @RequestBody PlateNumberDto plateNumberDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.error("Validation error in {}: {}", error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        VehicleOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> {
                    logger.error("Owner not found with ID: {}", ownerId);
                    return new RuntimeException("Owner not found");
                });

        if (plateNumberRepository.findByPlateNumber(plateNumberDto.getPlateNumber()).isPresent()) {
            logger.warn("Plate number already exists: {}", plateNumberDto.getPlateNumber());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Plate number already exists");
        }

        PlateNumber plateNumber = new PlateNumber();
        plateNumber.setOwner(owner);
        plateNumber.setPlateNumber(plateNumberDto.getPlateNumber());
        plateNumber.setIssuedDate(plateNumberDto.getIssuedDate());
        plateNumber.setInUse(plateNumberDto.isInUse());

        PlateNumber saved = plateNumberRepository.save(plateNumber);
        logger.info("Plate number registered for owner ID {}: {}", ownerId, plateNumberDto.getPlateNumber());

        PlateNumberDto responseDto = new PlateNumberDto();
        responseDto.setPlateNumber(saved.getPlateNumber());
        responseDto.setIssuedDate(saved.getIssuedDate());
        responseDto.setInUse(saved.isInUse());

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get plates for an owner")
    @GetMapping("/{ownerId}/plates")
    public ResponseEntity<?> getPlates(@PathVariable @Min(value = 1, message = "Owner ID must be positive") Long ownerId) {
        VehicleOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> {
                    logger.error("Owner not found with ID: {}", ownerId);
                    return new RuntimeException("Owner not found");
                });

        List<PlateNumber> plates = plateNumberRepository.findByOwner(owner);
        logger.info("Retrieved {} plates for owner ID {}", plates.size(), ownerId);

        return ResponseEntity.ok(plates.stream().map(plate -> {
            PlateNumberDto dto = new PlateNumberDto();
            dto.setPlateNumber(plate.getPlateNumber());
            dto.setIssuedDate(plate.getIssuedDate());
            dto.setInUse(plate.isInUse());
            return dto;
        }).toList());
    }
}
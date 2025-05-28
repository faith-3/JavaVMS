package rw.rra.vms.demo.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import rw.rra.vms.demo.dtos.LoginRequest;
import rw.rra.vms.demo.dtos.LoginResponse;
import rw.rra.vms.demo.dtos.SignupRequest;
import rw.rra.vms.demo.dtos.SignupResponse;
import rw.rra.vms.demo.Entities.User;
import rw.rra.vms.demo.Config.JwtUtil;
import rw.rra.vms.demo.Repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Handles authentication and user management (signup and login).")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    @Operation(summary = "User signup",
            security = @SecurityRequirement(name = ""),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = SignupResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate email/national ID")
            })
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.error("Validation error in {}: {}", error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            logger.warn("Signup attempt with duplicate email: {}", signupRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        if (userRepository.findByNationalId(signupRequest.getNationalId()).isPresent()) {
            logger.warn("Signup attempt with duplicate national ID: {}", signupRequest.getNationalId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("National ID already exists");
        }

        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setName(signupRequest.getName());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhone(signupRequest.getPhone());
        user.setRole(signupRequest.getRole());
        user.setNationalId(signupRequest.getNationalId());
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getEmail());

        SignupResponse response = new SignupResponse();
        response.setMessage("User registered successfully");
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setRole(user.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login",
            security = @SecurityRequirement(name = ""),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials or input"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.error("Validation error in {}: {}", error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception ex) {
            logger.error("Login failed for email {}: Invalid credentials", request.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        }

        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", request.getEmail());
                    return new UsernameNotFoundException("User not found with email: " + request.getEmail());
                });
        String token = jwtUtil.generateToken(existingUser);
        logger.info("Login successful for user: {}", existingUser.getEmail());

        LoginResponse response = new LoginResponse();
        response.setMessage("Login successful");
        response.setToken(token);
        return ResponseEntity.ok(response);
    }
}
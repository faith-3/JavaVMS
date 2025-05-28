package rw.rra.vms.demo.dtos;

import lombok.Data;

@Data
public class LoginResponse {
    private String message;
    private String token;
}
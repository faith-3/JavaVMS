package rw.rra.vms.demo.dtos;

import lombok.Data;

@Data
public class SignupResponse {
    private String message;
    private String email;
    private String name;
    private String role;
}

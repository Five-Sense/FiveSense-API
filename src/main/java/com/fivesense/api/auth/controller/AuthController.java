package com.fivesense.api.auth.controller;

import com.fivesense.api.auth.app.AuthService;
import com.fivesense.api.auth.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}
    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request){
        return service.login(request);
    }
}

package com.fivesense.api.auth.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank @Email @Size(max=255) String email,@NotBlank @Size(max=255) String password) {}
    public record LoginResponse(boolean authenticated,java.util.UUID userId,String name,String email,com.fivesense.api.users.domain.UserRole role) {}
}

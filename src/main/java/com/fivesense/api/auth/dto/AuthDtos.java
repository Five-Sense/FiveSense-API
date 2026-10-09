package com.fivesense.api.auth.dto;
import jakarta.validation.constraints.*;
public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank @Size(max=255) String username,@NotBlank @Size(max=255) String password) {}
    public record LoginResponse(boolean authenticated,java.util.UUID userId,String name,String username,String email,com.fivesense.api.users.domain.UserRole role) {}
    public record ForgotPasswordRequest(@NotBlank @Email @Size(max=255) String email,@NotBlank @Size(max=255) String newPassword) {}
    public record ForgotPasswordResponse(boolean updated,java.util.UUID userId,String email) {}
}

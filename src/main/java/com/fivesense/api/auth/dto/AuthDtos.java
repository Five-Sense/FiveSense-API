package com.fivesense.api.auth.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank @Email @Size(max=255) String email,@NotBlank @Size(max=255) String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record ChangePasswordRequest(@Size(max=255) String currentPassword,@NotBlank @Size(min=8,max=255) String newPassword,@NotBlank String confirmation) {}
    public record ResetRequest(@NotBlank @Email @Size(max=255) String email) {}
    public record ResetConfirmRequest(@NotBlank String token,@NotBlank @Size(min=8,max=255) String newPassword,@NotBlank String confirmation) {}
    public record TokenResponse(String accessToken,String refreshToken,String tokenType,long expiresIn,boolean passwordChangeRequired) {}
    public record MessageResponse(String message) {}
    public record BootstrapResponse(String email,String initialPassword) {}
}

package com.fivesense.api.users.dto;

import com.fivesense.api.users.domain.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {}
    public record CreateRequest(@NotBlank @Size(max=255) String name,@NotBlank @Email @Size(max=255) String email,@NotBlank @Size(max=255) String password,@NotNull UserRole role) {}
    public record UpdateRequest(@NotBlank @Size(max=255) String name,@NotBlank @Email @Size(max=255) String email,@NotNull UserStatus status) {}
    public record Response(UUID id,String name,String email,UserRole role,UserStatus status,Instant createdAt,Instant updatedAt) {}
}

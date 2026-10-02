package com.fivesense.api.problems.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public final class ProblemDtos {
    private ProblemDtos() {}
    public record UpsertRequest(@NotBlank @Size(max=20) String name,@NotBlank @Email @Size(max=255) String relatedEmail,@NotBlank @Size(max=255) String defaultResponse,Boolean active) {}
    public record Response(UUID id,String name,String relatedEmail,String defaultResponse,boolean active) {}
    public record Option(UUID id,String name) {}
}

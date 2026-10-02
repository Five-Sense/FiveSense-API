package com.fivesense.api.occurrences.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class OccurrenceDtos {
    private OccurrenceDtos() {}
    public record CreateRequest(@NotNull UUID problemId,@NotNull UUID materialId,@NotNull @Positive Integer affectedQuantity) {}
    public record Response(UUID id,UUID problemId,UUID materialId,UUID reportedByUserId,int affectedQuantity,Instant createdAt) {}
}

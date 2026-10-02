package com.fivesense.api.occurrences.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class OccurrenceDtos {
    private OccurrenceDtos() {}
    public record CreateRequest(@NotNull UUID problemId,@NotNull UUID materialId,@NotNull @Positive Integer affectedQuantity,@NotNull UUID reportedByUserId) {
        public CreateRequest(UUID problemId,UUID materialId,Integer affectedQuantity){this(problemId,materialId,affectedQuantity,new UUID(0,0));}
    }
    public record Response(UUID id,UUID problemId,UUID materialId,UUID reportedByUserId,int affectedQuantity,Instant createdAt) {}
}

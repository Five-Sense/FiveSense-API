package com.fivesense.api.occurrences.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OccurrenceDtos {
    /** Maximum number of images sent with one occurrence. */
    public static final int MAX_IMAGES=5;
    private OccurrenceDtos() {}
    /** JSON body. {@code images} is optional: each item carries the image as base64 text (see {@link ImageContent}). */
    public record CreateRequest(@NotNull UUID problemId,@NotNull UUID materialId,@NotNull @Positive Integer affectedQuantity,@NotNull UUID reportedByUserId,@Size(max=MAX_IMAGES) List<@NotNull @Valid ImageContent> images) {}
    /** Text fields of the {@code multipart/form-data} variant; the images travel as file or base64 parts named {@code images}. */
    public record CreateForm(@NotNull UUID problemId,@NotNull UUID materialId,@NotNull @Positive Integer affectedQuantity,@NotNull UUID reportedByUserId) {
        public CreateRequest toRequest(){return new CreateRequest(problemId,materialId,affectedQuantity,reportedByUserId,null);}
    }
    /** An image sent as text: base64 (standard or URL-safe), with or without a {@code data:image/...;base64,} prefix. {@code name} is optional. */
    public record ImageContent(@Size(max=255) String name,@NotBlank String data) {}
    public record Response(UUID id,UUID problemId,UUID materialId,UUID reportedByUserId,int affectedQuantity,Instant createdAt) {}
}

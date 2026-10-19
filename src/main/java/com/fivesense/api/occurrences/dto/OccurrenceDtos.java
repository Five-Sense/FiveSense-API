package com.fivesense.api.occurrences.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OccurrenceDtos {
    /** Maximum number of images attached to one occurrence (and accepted by one upload call). */
    public static final int MAX_IMAGES=5;
    private OccurrenceDtos() {}
    /** {@code imageIds} is the multi-image field; {@code imageId} is kept so clients sending a single image keep working. */
    public record CreateRequest(@NotNull UUID problemId,@NotNull UUID materialId,@NotNull @Positive Integer affectedQuantity,@NotNull UUID reportedByUserId,UUID imageId,@Size(max=MAX_IMAGES) List<@NotNull UUID> imageIds) {
        public CreateRequest(UUID problemId,UUID materialId,Integer affectedQuantity){this(problemId,materialId,affectedQuantity,new UUID(0,0),null,null);}
        public CreateRequest(UUID problemId,UUID materialId,Integer affectedQuantity,UUID reportedByUserId){this(problemId,materialId,affectedQuantity,reportedByUserId,null,null);}
        public CreateRequest(UUID problemId,UUID materialId,Integer affectedQuantity,UUID reportedByUserId,UUID imageId){this(problemId,materialId,affectedQuantity,reportedByUserId,imageId,null);}
    }
    public record Response(UUID id,UUID problemId,UUID materialId,UUID reportedByUserId,int affectedQuantity,Instant createdAt) {}
    /** {@code imageIds} lists every stored image in upload order; {@code imageId} repeats the first one for single-image clients. */
    public record ImageUploadResponse(UUID imageId,List<UUID> imageIds) {
        public static ImageUploadResponse of(List<UUID> ids){return new ImageUploadResponse(ids.isEmpty()?null:ids.get(0),List.copyOf(ids));}
    }
    /** JSON upload: images sent as text (base64, with or without a {@code data:image/...;base64,} prefix). */
    public record ImageBatchRequest(@NotEmpty @Size(max=MAX_IMAGES) List<@NotNull @Valid ImageContent> images) {}
    public record ImageContent(@Size(max=255) String name,@NotBlank String data) {}
}

package com.fivesense.api.materials.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public final class MaterialDtos {
    private MaterialDtos() {}
    public record UpsertRequest(@NotBlank @Size(max=55) String name,@NotNull @Min(0) @Max(999) Integer stockQuantity,@NotNull @Min(0) @Max(999) Integer minimumStock,Boolean active) {}
    public record Response(UUID id,String name,int stockQuantity,int minimumStock,boolean active,boolean lowStock) {}
    public record StockView(UUID id,String name,int stockQuantity,int minimumStock,boolean lowStock) {}
    public record Option(UUID id,String name) {}
}

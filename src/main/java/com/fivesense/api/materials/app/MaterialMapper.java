package com.fivesense.api.materials.app;

import com.fivesense.api.materials.domain.Material;
import com.fivesense.api.materials.dto.MaterialDtos;
import org.mapstruct.*;

@Mapper(componentModel="spring")
public interface MaterialMapper {
    @Mapping(target="lowStock", expression="java(material.getStockQuantity() <= material.getMinimumStock())")
    MaterialDtos.Response toResponse(Material material);
    @Mapping(target="lowStock", expression="java(material.getStockQuantity() <= material.getMinimumStock())")
    MaterialDtos.StockView toStockView(Material material);
    MaterialDtos.Option toOption(Material material);
}

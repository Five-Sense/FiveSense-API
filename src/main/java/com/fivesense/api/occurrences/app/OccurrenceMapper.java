package com.fivesense.api.occurrences.app;

import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface OccurrenceMapper { OccurrenceDtos.Response toResponse(Occurrence occurrence); }

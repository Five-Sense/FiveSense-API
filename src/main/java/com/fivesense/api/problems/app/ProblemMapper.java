package com.fivesense.api.problems.app;

import com.fivesense.api.problems.domain.Problem;
import com.fivesense.api.problems.dto.ProblemDtos;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface ProblemMapper { ProblemDtos.Response toResponse(Problem problem); ProblemDtos.Option toOption(Problem problem); }

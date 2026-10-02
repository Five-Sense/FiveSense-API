package com.fivesense.api.teams.app;

import com.fivesense.api.teams.domain.Team;
import com.fivesense.api.teams.dto.TeamDtos;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface TeamMapper { TeamDtos.Response toResponse(Team team); }

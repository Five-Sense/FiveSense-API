package com.fivesense.api.teams.app;

import com.fivesense.api.teams.domain.Team;
import com.fivesense.api.teams.dto.TeamDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface TeamMapper {
    @Mapping(target="startTime",dateFormat="HH:mm")
    @Mapping(target="endTime",dateFormat="HH:mm")
    TeamDtos.Response toResponse(Team team);
}

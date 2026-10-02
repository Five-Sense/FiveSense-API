package com.fivesense.api.teams.app;

import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.teams.domain.*;
import com.fivesense.api.teams.dto.TeamDtos;
import com.fivesense.api.teams.infra.TeamRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class TeamService {
    private final TeamRepository teams;private final TeamMapper mapper;
    public TeamService(TeamRepository teams,TeamMapper mapper){this.teams=teams;this.mapper=mapper;}
    @Transactional(readOnly=true) public Page<TeamDtos.Response> list(Pageable pageable){return teams.findAll(pageable).map(mapper::toResponse);}
    @Transactional(readOnly=true) public TeamDtos.Response get(UUID id){return mapper.toResponse(teams.findById(id).orElseThrow(()->ApiException.notFound("Team")));}
    @Transactional public TeamDtos.Response create(TeamDtos.UpsertRequest request){return mapper.toResponse(teams.save(new Team(clean(request.name()),clean(request.code()),safe(request.representatives()),safe(request.schedule()),Instant.now())));}
    @Transactional public TeamDtos.Response update(UUID id,TeamDtos.UpsertRequest request){Team team=teams.findById(id).orElseThrow(()->ApiException.notFound("Team"));team.update(clean(request.name()),clean(request.code()),safe(request.representatives()),safe(request.schedule()),Instant.now());return mapper.toResponse(team);}
    @Transactional public TeamDtos.Response updateStatus(UUID id,TeamStatus status){Team team=teams.findById(id).orElseThrow(()->ApiException.notFound("Team"));team.setStatus(status,Instant.now());return mapper.toResponse(team);}
    @Transactional public void delete(UUID id){Team team=teams.findById(id).orElseThrow(()->ApiException.notFound("Team"));teams.delete(team);}
    private static String clean(String value){return value==null||value.isBlank()?null:value.trim();}
    private static String safe(String value){return value==null?"":value.trim();}
}

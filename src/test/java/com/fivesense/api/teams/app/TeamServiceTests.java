package com.fivesense.api.teams.app;

import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.teams.domain.*;
import com.fivesense.api.teams.dto.TeamDtos;
import com.fivesense.api.teams.infra.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamServiceTests {
    private TeamRepository teams;private TeamMapper mapper;private TeamService service;
    @BeforeEach void setup(){teams=mock(TeamRepository.class);mapper=mock(TeamMapper.class);service=new TeamService(teams,mapper);}

    @Test void listMapsThePage(){
        Team team=new Team("A",null,"Ana","8h",Instant.now());when(teams.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(team)));
        when(mapper.toResponse(team)).thenReturn(response());assertThat(service.list(PageRequest.of(0,20))).hasSize(1);verify(mapper).toResponse(team);
    }
    @Test void getMapsTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"","",Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));when(mapper.toResponse(team)).thenReturn(response());
        assertThat(service.get(id)).isNotNull();when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.get(id)).isInstanceOf(ApiException.class);
    }
    @Test void createTrimsTextAndMapsSavedTeam(){
        TeamDtos.UpsertRequest request=new TeamDtos.UpsertRequest(" A ",null," Ana "," turno ");when(teams.save(any(Team.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(response());
        service.create(request);verify(teams).save(argThat(t->t.getName().equals("A")&&t.getRepresentatives().equals("Ana")&&t.getSchedule().equals("turno")));
    }
    @Test void updateMutatesTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"","",Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));when(mapper.toResponse(team)).thenReturn(response());
        service.update(id,new TeamDtos.UpsertRequest("B",null,"Rep","9h"));assertThat(team.getName()).isEqualTo("B");
        when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.update(id,new TeamDtos.UpsertRequest("B",null,"",""))).isInstanceOf(ApiException.class);
    }
    @Test void updateStatusMutatesTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"","",Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));when(mapper.toResponse(team)).thenReturn(response());
        service.updateStatus(id,TeamStatus.DOING_5S);assertThat(team.getStatus()).isEqualTo(TeamStatus.DOING_5S);
        when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.updateStatus(id,TeamStatus.NOT_DOING_5S)).isInstanceOf(ApiException.class);
    }
    @Test void deleteRemovesExistingTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"","",Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));service.delete(id);verify(teams).delete(team);
        when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.delete(id)).isInstanceOf(ApiException.class);
    }
    private static TeamDtos.Response response(){return new TeamDtos.Response(UUID.randomUUID(),"A",null,"","",TeamStatus.NOT_DOING_5S);}
}

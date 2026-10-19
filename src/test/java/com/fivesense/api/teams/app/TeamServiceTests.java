package com.fivesense.api.teams.app;

import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.teams.domain.*;
import com.fivesense.api.teams.dto.TeamDtos;
import com.fivesense.api.teams.infra.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamServiceTests {
    private TeamRepository teams;private TeamMapper mapper;private TeamService service;
    @BeforeEach void setup(){teams=mock(TeamRepository.class);mapper=mock(TeamMapper.class);service=new TeamService(teams,mapper);}

    @Test void listMapsThePage(){
        Team team=new Team("A",null,"Ana",LocalTime.of(8,0),LocalTime.of(10,0),Instant.now());when(teams.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(team)));
        when(mapper.toResponse(team)).thenReturn(response());assertThat(service.list(PageRequest.of(0,20))).hasSize(1);verify(mapper).toResponse(team);
    }
    @Test void getMapsTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"",null,null,Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));when(mapper.toResponse(team)).thenReturn(response());
        assertThat(service.get(id)).isNotNull();when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.get(id)).isInstanceOf(ApiException.class);
    }
    @Test void createTrimsTextAndMapsSavedTeam(){
        TeamDtos.UpsertRequest request=new TeamDtos.UpsertRequest(" A ",null," Ana "," 07:30 ","09:00");when(teams.save(any(Team.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(response());
        service.create(request);verify(teams).save(argThat(t->t.getName().equals("A")&&t.getRepresentatives().equals("Ana")&&t.getStartTime().equals(LocalTime.of(7,30))&&t.getEndTime().equals(LocalTime.of(9,0))));
    }
    @Test void createAcceptsATeamWithoutSchedule(){
        when(teams.save(any(Team.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(response());
        service.create(new TeamDtos.UpsertRequest("A",null,null,"  ",null));verify(teams).save(argThat(t->t.getStartTime()==null&&t.getEndTime()==null));
    }
    @Test void createAndUpdateRejectIncompleteOrInvertedIntervals(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"",LocalTime.of(7,0),LocalTime.of(9,0),Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));
        for(var request:List.of(new TeamDtos.UpsertRequest("A",null,null,"07:00",null),new TeamDtos.UpsertRequest("A",null,null,null,"09:00"),
                new TeamDtos.UpsertRequest("A",null,null,"09:00","09:00"),new TeamDtos.UpsertRequest("A",null,null,"10:00","09:00"),new TeamDtos.UpsertRequest("A",null,null,"7:00","09:00"))){
            assertThatThrownBy(()->service.create(request)).isInstanceOf(ApiException.class).satisfies(ex->assertThat(((ApiException)ex).getStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST));
            assertThatThrownBy(()->service.update(id,request)).isInstanceOf(ApiException.class);
        }
        verify(teams,never()).save(any());assertThat(team.getStartTime()).isEqualTo(LocalTime.of(7,0));assertThat(team.getEndTime()).isEqualTo(LocalTime.of(9,0));
    }
    @Test void updateMutatesTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"",null,null,Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));when(mapper.toResponse(team)).thenReturn(response());
        service.update(id,new TeamDtos.UpsertRequest("B",null,"Rep","09:00","11:15"));assertThat(team.getName()).isEqualTo("B");assertThat(team.getStartTime()).isEqualTo(LocalTime.of(9,0));assertThat(team.getEndTime()).isEqualTo(LocalTime.of(11,15));
        when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.update(id,new TeamDtos.UpsertRequest("B",null,"",null,null))).isInstanceOf(ApiException.class);
    }
    @Test void updateStatusMutatesTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"",null,null,Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));when(mapper.toResponse(team)).thenReturn(response());
        service.updateStatus(id,TeamStatus.DOING_5S);assertThat(team.getStatus()).isEqualTo(TeamStatus.DOING_5S);
        when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.updateStatus(id,TeamStatus.NOT_DOING_5S)).isInstanceOf(ApiException.class);
    }
    @Test void deleteRemovesExistingTeamAndRejectsMissing(){
        UUID id=UUID.randomUUID();Team team=new Team("A",null,"",null,null,Instant.now());when(teams.findById(id)).thenReturn(Optional.of(team));service.delete(id);verify(teams).delete(team);
        when(teams.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.delete(id)).isInstanceOf(ApiException.class);
    }
    private static TeamDtos.Response response(){return new TeamDtos.Response(UUID.randomUUID(),"A",null,"","07:00","09:00",TeamStatus.NOT_DOING_5S);}
}

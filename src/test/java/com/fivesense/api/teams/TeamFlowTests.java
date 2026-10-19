package com.fivesense.api.teams;

import com.fivesense.api.shared.error.ApiExceptionHandler;
import com.fivesense.api.teams.app.TeamMapperImpl;
import com.fivesense.api.teams.app.TeamService;
import com.fivesense.api.teams.controller.TeamController;
import com.fivesense.api.teams.domain.Team;
import com.fivesense.api.teams.infra.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

/** Web-layer flow of the team schedule interval: real controller, service and mapper; only the repository is mocked. */
class TeamFlowTests {
    private TeamRepository teams;
    private MockMvc mvc;

    @BeforeEach void setup() {
        teams = mock(TeamRepository.class);
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        mvc = MockMvcBuilders.standaloneSetup(new TeamController(new TeamService(teams, new TeamMapperImpl()))).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    private static String body(String start, String end) {
        return "{\"name\":\"Produção A\",\"representatives\":\"João; Maria\""
                + (start == null ? "" : ",\"startTime\":\"" + start + "\"") + (end == null ? "" : ",\"endTime\":\"" + end + "\"") + "}";
    }

    @Test void createStoresTheIntervalAndReturnsItAsHoursAndMinutes() throws Exception {
        mvc.perform(post("/api/v1/teams").contentType(MediaType.APPLICATION_JSON).content(body("07:30", "09:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("07:30"))
                .andExpect(jsonPath("$.endTime").value("09:00"))
                .andExpect(jsonPath("$.schedule").doesNotExist());
        var saved = org.mockito.ArgumentCaptor.forClass(Team.class);
        verify(teams).save(saved.capture());
        assertThat(saved.getValue().getStartTime()).isEqualTo(LocalTime.of(7, 30));
        assertThat(saved.getValue().getEndTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test void createWithoutScheduleReturnsNullTimes() throws Exception {
        mvc.perform(post("/api/v1/teams").contentType(MediaType.APPLICATION_JSON).content(body(null, null)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.startTime").doesNotExist()).andExpect(jsonPath("$.endTime").doesNotExist());
    }

    @Test void invalidSchedulesAreRejectedWithBadRequestAndNothingIsSaved() throws Exception {
        for (String[] times : new String[][]{{"Turno manhã", "Turno tarde"}, {"7:30", "09:00"}, {"07:30", null}, {null, "09:00"}, {"09:00", "09:00"}, {"10:00", "09:00"}}) {
            mvc.perform(post("/api/v1/teams").contentType(MediaType.APPLICATION_JSON).content(body(times[0], times[1])))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isArray());
        }
        mvc.perform(post("/api/v1/teams").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"A\",\"schedule\":\"Turno manhã\"}")).andExpect(status().isCreated()); // old field is simply ignored
        verify(teams, times(1)).save(any());
    }
}

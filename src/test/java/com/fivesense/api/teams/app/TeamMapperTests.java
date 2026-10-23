package com.fivesense.api.teams.app;

import com.fivesense.api.teams.domain.Team;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMapperTests {
    private final TeamMapper mapper = new TeamMapperImpl();

    @Test void formatsStartAndEndTimeAsHoursAndMinutes() {
        var response = mapper.toResponse(new Team("Produção A", "PROD-A", "Carlos", LocalTime.of(7, 30), LocalTime.of(9, 5, 40), Instant.now()));
        assertThat(response.startTime()).isEqualTo("07:30");
        assertThat(response.endTime()).isEqualTo("09:05");
        assertThat(response.name()).isEqualTo("Produção A");
    }

    @Test void leavesTheTimesNullWhenTheTeamHasNoSchedule() {
        var response = mapper.toResponse(new Team("A", null, "", null, null, Instant.now()));
        assertThat(response.startTime()).isNull();
        assertThat(response.endTime()).isNull();
    }
}

package com.fivesense.api.teams.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TeamDtosTests {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static Set<ConstraintViolation<TeamDtos.UpsertRequest>> validate(String start, String end) {
        return VALIDATOR.validate(new TeamDtos.UpsertRequest("Equipe A", null, "João; Maria", start, end));
    }

    private static boolean hasErrorOn(Set<ConstraintViolation<TeamDtos.UpsertRequest>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }

    @ParameterizedTest
    @ValueSource(strings = {"07:30", "00:00", "23:59", "13:05", " 07:30 "})
    @NullAndEmptySource
    void acceptsTimesInHoursAndMinutesFormatForBothFields(String time) {
        assertThat(hasErrorOn(validate(time, null), "startTime")).isFalse();
        assertThat(hasErrorOn(validate(null, time), "endTime")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Turno manhã", "Turno tarde", "7:30", "24:00", "12:60", "07:30:00", "07h30", "07:30 - 09:00", "Seg a Sex, 07:00"})
    void rejectsTextShiftsAndAnythingThatIsNotATime(String time) {
        assertThat(hasErrorOn(validate(time, "23:00"), "startTime")).isTrue();
        assertThat(hasErrorOn(validate("00:00", time), "endTime")).isTrue();
    }

    @Test void acceptsAValidIntervalOrNoScheduleAtAll() {
        assertThat(validate("07:30", "09:00")).isEmpty();
        assertThat(validate("00:00", "23:59")).isEmpty();
        assertThat(validate(null, null)).isEmpty();
        assertThat(validate("", " ")).isEmpty();
    }

    @Test void requiresStartAndEndTogetherWithEndAfterStart() {
        assertThat(validate("07:30", null)).isNotEmpty();
        assertThat(validate(null, "09:00")).isNotEmpty();
        assertThat(validate("09:00", "09:00")).isNotEmpty();
        assertThat(validate("10:00", "09:00")).isNotEmpty();
    }

    @Test void malformedTimeReportsOnlyItsFormatError() {
        var violations = validate("7:30", "09:00");
        assertThat(hasErrorOn(violations, "startTime")).isTrue();
        assertThat(hasErrorOn(violations, "validInterval")).isFalse();
    }

    @Test void stillRequiresNameOrCode() {
        assertThat(VALIDATOR.validate(new TeamDtos.UpsertRequest(null, " ", "", "07:30", "09:00"))).hasSize(1);
        assertThat(VALIDATOR.validate(new TeamDtos.UpsertRequest(null, "A-01", "", "07:30", "09:00"))).isEmpty();
    }
}

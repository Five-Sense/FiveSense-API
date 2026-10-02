package com.fivesense.api.teams.dto;

import com.fivesense.api.teams.domain.TeamStatus;
import jakarta.validation.constraints.*;
import java.util.UUID;

public final class TeamDtos {
    private TeamDtos() {}
    public record UpsertRequest(@Size(max=255) String name,@Size(max=255) String code,@Size(max=255) String representatives,@Size(max=255) String schedule) {
        @AssertTrue(message="name or code is required") public boolean hasIdentity(){return (name!=null&&!name.isBlank())||(code!=null&&!code.isBlank());}
    }
    public record StatusRequest(@NotNull TeamStatus status) {}
    public record Response(UUID id,String name,String code,String representatives,String schedule,TeamStatus status) {}
}

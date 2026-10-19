package com.fivesense.api.teams.dto;

import com.fivesense.api.teams.domain.TeamStatus;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public final class TeamDtos {
    private TeamDtos() {}
    /** A 24-hour time such as {@code 07:30}; surrounding spaces and an empty value (not informed) are accepted. */
    public static final String TIME_PATTERN="^\\s*(([01]\\d|2[0-3]):[0-5]\\d)?\\s*$";
    private static final String TIME_MESSAGE="must be a time in HH:mm format (00:00-23:59), e.g. 07:30";

    /** {@code startTime} and {@code endTime} (HH:mm) form the team's schedule interval: both or neither, end after start. */
    public record UpsertRequest(@Size(max=255) String name,@Size(max=255) String code,@Size(max=255) String representatives,
                                @Pattern(regexp=TIME_PATTERN,message=TIME_MESSAGE) String startTime,
                                @Pattern(regexp=TIME_PATTERN,message=TIME_MESSAGE) String endTime) {
        @AssertTrue(message="name or code is required") public boolean hasIdentity(){return (name!=null&&!name.isBlank())||(code!=null&&!code.isBlank());}
        @AssertTrue(message="startTime and endTime must be informed together, with endTime after startTime") public boolean hasValidInterval(){
            boolean noStart=startTime==null||startTime.isBlank(),noEnd=endTime==null||endTime.isBlank();
            if(noStart&&noEnd)return true;
            if(noStart!=noEnd)return false;
            LocalTime start=parse(startTime),end=parse(endTime);
            return start==null||end==null||end.isAfter(start); // malformed values are reported by their own @Pattern
        }
        private static LocalTime parse(String value){try{return LocalTime.parse(value.trim());}catch(DateTimeParseException ex){return null;}}
    }
    public record StatusRequest(@NotNull TeamStatus status) {}
    public record Response(UUID id,String name,String code,String representatives,String startTime,String endTime,TeamStatus status) {}
}

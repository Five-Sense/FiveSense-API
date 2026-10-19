package com.fivesense.api.teams.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity @Table(name = "team")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(length = 255) private String name;
    @Column(length = 255) private String code;
    @Column(nullable = false, length = 255) private String representatives = "";
    @Column(name = "start_time") private LocalTime startTime;
    @Column(name = "end_time") private LocalTime endTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TeamStatus status = TeamStatus.NOT_DOING_5S;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected Team() {}
    public Team(String name, String code, String representatives, LocalTime startTime, LocalTime endTime, Instant now) {
        requireValidInterval(startTime, endTime);
        this.name=name; this.code=code; this.representatives=representatives; this.startTime=startTime; this.endTime=endTime; this.createdAt=now; this.updatedAt=now;
    }
    public void update(String name, String code, String representatives, LocalTime startTime, LocalTime endTime, Instant now) {
        requireValidInterval(startTime, endTime);
        this.name=name; this.code=code; this.representatives=representatives; this.startTime=startTime; this.endTime=endTime; this.updatedAt=now;
    }
    public void setStatus(TeamStatus status, Instant now) { this.status=status; this.updatedAt=now; }

    /** The schedule is an interval within one day: both times or neither, and the end after the start. */
    private static void requireValidInterval(LocalTime start, LocalTime end) {
        if ((start == null) != (end == null)) throw new IllegalArgumentException("startTime and endTime must be informed together");
        if (start != null && !end.isAfter(start)) throw new IllegalArgumentException("endTime must be after startTime");
    }

    public UUID getId(){return id;} public String getName(){return name;} public String getCode(){return code;}
    public String getRepresentatives(){return representatives;} public LocalTime getStartTime(){return startTime;} public LocalTime getEndTime(){return endTime;} public TeamStatus getStatus(){return status;}
}

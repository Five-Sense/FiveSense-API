package com.fivesense.api.teams.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "team")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(length = 255) private String name;
    @Column(length = 255) private String code;
    @Column(nullable = false, length = 255) private String representatives = "";
    @Column(nullable = false, length = 255) private String schedule = "";
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TeamStatus status = TeamStatus.NOT_DOING_5S;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected Team() {}
    public Team(String name, String code, String representatives, String schedule, Instant now) {
        this.name=name; this.code=code; this.representatives=representatives; this.schedule=schedule; this.createdAt=now; this.updatedAt=now;
    }
    public void update(String name, String code, String representatives, String schedule, Instant now) {
        this.name=name; this.code=code; this.representatives=representatives; this.schedule=schedule; this.updatedAt=now;
    }
    public void setStatus(TeamStatus status, Instant now) { this.status=status; this.updatedAt=now; }
    public UUID getId(){return id;} public String getName(){return name;} public String getCode(){return code;}
    public String getRepresentatives(){return representatives;} public String getSchedule(){return schedule;} public TeamStatus getStatus(){return status;}
}

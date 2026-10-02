package com.fivesense.api.problems.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "problem")
public class Problem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true, length = 20) private String name;
    @Column(name="related_email", nullable=false, length=255) private String relatedEmail;
    @Column(name="default_response", nullable=false, length=255) private String defaultResponse;
    @Column(nullable=false) private boolean active = true;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    protected Problem() {}
    public Problem(String name,String relatedEmail,String defaultResponse,Instant now){this.name=name;this.relatedEmail=relatedEmail;this.defaultResponse=defaultResponse;this.createdAt=now;this.updatedAt=now;}
    public void update(String name,String relatedEmail,String defaultResponse,boolean active,Instant now){this.name=name;this.relatedEmail=relatedEmail;this.defaultResponse=defaultResponse;this.active=active;this.updatedAt=now;}
    public UUID getId(){return id;} public String getName(){return name;} public String getRelatedEmail(){return relatedEmail;} public String getDefaultResponse(){return defaultResponse;} public boolean isActive(){return active;}
}

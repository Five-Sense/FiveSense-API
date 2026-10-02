package com.fivesense.api.occurrences.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="occurrence")
public class Occurrence {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="problem_id",nullable=false) private UUID problemId;
    @Column(name="material_id",nullable=false) private UUID materialId;
    @Column(name="reported_by_user_id",nullable=false) private UUID reportedByUserId;
    @Column(name="affected_quantity",nullable=false) private int affectedQuantity;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected Occurrence() {}
    public Occurrence(UUID problemId,UUID materialId,UUID reportedByUserId,int affectedQuantity,Instant now){this.problemId=problemId;this.materialId=materialId;this.reportedByUserId=reportedByUserId;this.affectedQuantity=affectedQuantity;this.createdAt=now;}
    public UUID getId(){return id;} public UUID getProblemId(){return problemId;} public UUID getMaterialId(){return materialId;} public UUID getReportedByUserId(){return reportedByUserId;} public int getAffectedQuantity(){return affectedQuantity;} public Instant getCreatedAt(){return createdAt;}
}

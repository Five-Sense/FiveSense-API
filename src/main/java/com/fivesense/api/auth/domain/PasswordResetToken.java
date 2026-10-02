package com.fivesense.api.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="password_reset_token")
public class PasswordResetToken {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="user_id",nullable=false) private UUID userId;
    @Column(name="token_hash",nullable=false,unique=true,length=255) private String tokenHash;
    @Column(name="expires_at",nullable=false) private Instant expiresAt;
    @Column(name="used_at") private Instant usedAt;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected PasswordResetToken() {}
    public PasswordResetToken(UUID userId,String tokenHash,Instant expiresAt,Instant now){this.userId=userId;this.tokenHash=tokenHash;this.expiresAt=expiresAt;this.createdAt=now;}
    public void markUsed(Instant now){this.usedAt=now;}
    public UUID getId(){return id;} public UUID getUserId(){return userId;} public String getTokenHash(){return tokenHash;} public Instant getExpiresAt(){return expiresAt;} public Instant getUsedAt(){return usedAt;}
}

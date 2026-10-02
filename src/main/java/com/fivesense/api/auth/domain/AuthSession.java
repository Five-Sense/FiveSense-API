package com.fivesense.api.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="auth_session")
public class AuthSession {
    @Id private UUID id;
    @Column(name="user_id",nullable=false) private UUID userId;
    @Column(name="refresh_token_hash",unique=true,length=255) private String refreshTokenHash;
    @Enumerated(EnumType.STRING) @Column(name="session_type",nullable=false,length=20) private SessionType sessionType;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="last_used_at",nullable=false) private Instant lastUsedAt;
    @Column(name="expires_at",nullable=false) private Instant expiresAt;
    @Column(name="revoked_at") private Instant revokedAt;
    protected AuthSession() {}
    public AuthSession(UUID id, UUID userId, String refreshTokenHash, SessionType sessionType, Instant now, Instant expiresAt){
        this.id=id;this.userId=userId;this.refreshTokenHash=refreshTokenHash;this.sessionType=sessionType;this.createdAt=now;this.lastUsedAt=now;this.expiresAt=expiresAt;
    }
    public void rotate(String hash,Instant now){this.refreshTokenHash=hash;this.lastUsedAt=now;}
    public void touch(Instant now){this.lastUsedAt=now;}
    public void revoke(Instant now){this.revokedAt=now;}
    public UUID getId(){return id;} public UUID getUserId(){return userId;} public String getRefreshTokenHash(){return refreshTokenHash;}
    public SessionType getSessionType(){return sessionType;} public Instant getCreatedAt(){return createdAt;} public Instant getLastUsedAt(){return lastUsedAt;} public Instant getExpiresAt(){return expiresAt;} public Instant getRevokedAt(){return revokedAt;}
}

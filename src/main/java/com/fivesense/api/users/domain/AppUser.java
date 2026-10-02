package com.fivesense.api.users.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserRole role;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserStatus status;
    @Column(name = "auth_version", nullable = false) private int authVersion;
    @Column(name = "credential_email_sent_at") private Instant credentialEmailSentAt;
    @Column(name = "credential_email_window_started_at") private Instant credentialEmailWindowStartedAt;
    @Column(name = "credential_email_count", nullable = false) private int credentialEmailCount;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected AppUser() {}
    public AppUser(String name, String email, String passwordHash, UserRole role, UserStatus status, Instant now) {
        this.name = name; this.email = email; this.passwordHash = passwordHash; this.role = role; this.status = status;
        this.createdAt = now; this.updatedAt = now;
    }
    public void update(String name, String email, UserRole role, UserStatus status, Instant now) {
        this.name = name; this.email = email; this.role = role; this.status = status; this.authVersion++; this.updatedAt = now;
    }
    public void changePassword(String passwordHash, UserStatus status, Instant now) {
        this.passwordHash = passwordHash; this.status = status; this.authVersion++; this.updatedAt = now;
    }
    public boolean canResendCredentialEmail(Instant now) {
        if (credentialEmailSentAt != null && credentialEmailSentAt.plusSeconds(60).isAfter(now)) return false;
        return credentialEmailWindowStartedAt == null || credentialEmailWindowStartedAt.plusSeconds(3600).isBefore(now) || credentialEmailCount < 3;
    }
    public void recordCredentialEmail(Instant now) {
        if (credentialEmailWindowStartedAt == null || credentialEmailWindowStartedAt.plusSeconds(3600).isBefore(now)) { credentialEmailWindowStartedAt=now; credentialEmailCount=0; }
        credentialEmailCount++; credentialEmailSentAt = now; this.updatedAt = now;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public int getAuthVersion() { return authVersion; }
    public Instant getCredentialEmailSentAt() { return credentialEmailSentAt; }
    public Instant getCredentialEmailWindowStartedAt() { return credentialEmailWindowStartedAt; }
    public int getCredentialEmailCount() { return credentialEmailCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

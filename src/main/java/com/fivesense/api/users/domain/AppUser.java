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
    @Column(nullable = false, unique = true, length = 255) private String username;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @Column(name = "password", nullable = false, length = 255) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserRole role;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected AppUser() {}
    public AppUser(String name, String username, String email, String password, UserRole role, UserStatus status, Instant now) {
        this.name = name; this.username = username; this.email = email; this.password = password; this.role = role; this.status = status;
        this.createdAt = now; this.updatedAt = now;
    }
    public void update(String name, String username, String email, UserRole role, UserStatus status, Instant now) {
        this.name = name; this.username = username; this.email = email; this.role = role; this.status = status; this.updatedAt = now;
    }
    public void changePassword(String password, Instant now) {
        this.password = password; this.updatedAt = now;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

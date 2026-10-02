package com.fivesense.api.auth.infra;

import com.fivesense.api.auth.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}

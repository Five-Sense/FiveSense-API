package com.fivesense.api.auth.infra;

import com.fivesense.api.auth.domain.AuthSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    @Query("select s from AuthSession s where s.id=:id and s.userId=:userId") Optional<AuthSession> findOwned(@Param("id") UUID id,@Param("userId") UUID userId);
    List<AuthSession> findByUserIdAndRevokedAtIsNull(UUID userId);
    List<AuthSession> findByUserIdAndSessionTypeAndRevokedAtIsNullOrderByCreatedAtAsc(UUID userId, com.fivesense.api.auth.domain.SessionType sessionType);
    @Modifying @Query("update AuthSession s set s.revokedAt=:now where s.userId=:userId and s.revokedAt is null") int revokeAll(@Param("userId") UUID userId,@Param("now") Instant now);
}

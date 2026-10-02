package com.fivesense.api.auth.app;

import com.fivesense.api.auth.domain.AuthSession;
import com.fivesense.api.auth.domain.SessionType;
import com.fivesense.api.auth.infra.AuthSessionRepository;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.AppUser;
import com.fivesense.api.users.domain.UserRole;
import com.fivesense.api.users.domain.UserStatus;
import com.fivesense.api.users.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SessionServiceTests {
    private AuthSessionRepository sessions;
    private UserRepository users;
    private SessionService service;

    @BeforeEach void setUp(){sessions=mock(AuthSessionRepository.class);users=mock(UserRepository.class);service=new SessionService(sessions,users);}

    @Test void viewerCreationRevokesOldestWhenFiveSessionsAreAlreadyActive() throws Exception {
        Instant now=Instant.parse("2026-01-01T00:00:00Z");AppUser viewer=user(UserRole.VIEWER);
        var active=new ArrayList<AuthSession>();
        for(int i=0;i<5;i++)active.add(new AuthSession(UUID.randomUUID(),viewer.getId(),"hash"+i,SessionType.REFRESHABLE,now.plusSeconds(i),now.plusSeconds(86400)));
        when(sessions.findByUserIdAndSessionTypeAndRevokedAtIsNullOrderByCreatedAtAsc(viewer.getId(),SessionType.REFRESHABLE)).thenReturn(active);
        service.create(viewer,now,false);
        assertThat(active.get(0).getRevokedAt()).isEqualTo(now);
        assertThat(active.subList(1,active.size())).allSatisfy(session->assertThat(session.getRevokedAt()).isNull());
        verify(sessions).save(any(AuthSession.class));
    }

    @Test void nonViewerGetsAccessOnlySessionWithoutRefreshToken() throws Exception {
        var issue=service.create(user(UserRole.MANAGER),Instant.now(),false);
        assertThat(issue.session().getSessionType()).isEqualTo(SessionType.ACCESS_ONLY);
        assertThat(issue.refreshToken()).isNull();
    }

    @Test void firstAccessGetsShortPasswordChangeSession() throws Exception {
        var issue=service.create(user(UserRole.ADMIN),Instant.now(),true);
        assertThat(issue.session().getSessionType()).isEqualTo(SessionType.PASSWORD_CHANGE);
        assertThat(issue.session().getExpiresAt()).isEqualTo(issue.session().getCreatedAt().plusSeconds(300));
    }

    @Test void rotationReplacesStoredHashAndReuseRevokesTheSession() throws Exception {
        Instant now=Instant.now();UUID id=UUID.randomUUID();String raw=id+".old-token";
        AppUser viewer=user(UserRole.VIEWER);AuthSession session=new AuthSession(id,viewer.getId(),SessionService.hash(raw),SessionType.REFRESHABLE,now,now.plusSeconds(86400));
        when(sessions.findById(id)).thenReturn(Optional.of(session));when(users.findById(viewer.getId())).thenReturn(Optional.of(viewer));
        var rotated=service.rotate(raw,now.plusSeconds(1));
        assertThat(rotated.refreshToken()).isNotEqualTo(raw);
        assertThat(session.getRefreshTokenHash()).isEqualTo(SessionService.hash(rotated.refreshToken()));
        assertThatThrownBy(()->service.rotate(raw,now.plusSeconds(2))).isInstanceOf(ApiException.class);
        assertThat(session.getRevokedAt()).isEqualTo(now.plusSeconds(2));
    }

    @Test void invalidSessionTokenIsRejected(){
        assertThatThrownBy(()->service.rotate("malformed",Instant.now())).isInstanceOf(ApiException.class);
    }

    @Test void revokeAndRevokeAllDelegateToRepository(){
        UUID id=UUID.randomUUID(),userId=UUID.randomUUID();Instant now=Instant.now();
        AuthSession owned=new AuthSession(id,userId,null,SessionType.ACCESS_ONLY,now,now.plusSeconds(3600));
        when(sessions.findOwned(id,userId)).thenReturn(Optional.of(owned));
        service.revoke(id,userId,now);service.revokeAll(userId,now);
        assertThat(owned.getRevokedAt()).isEqualTo(now);verify(sessions).revokeAll(userId,now);
    }

    @Test void activeSessionRequiresValidTimeWindow(){
        UUID id=UUID.randomUUID(),userId=UUID.randomUUID();Instant now=Instant.now();
        AuthSession active=new AuthSession(id,userId,null,SessionType.ACCESS_ONLY,now,now.plusSeconds(3600));
        when(sessions.findOwned(id,userId)).thenReturn(Optional.of(active));
        assertThat(service.isActive(id,userId,now)).isTrue();
        assertThat(service.isActive(id,userId,now.plusSeconds(8*3600+1))).isFalse();
    }

    @Test void refreshHashIsStableAndOneWay(){
        assertThat(SessionService.hash("secret")).isEqualTo(SessionService.hash("secret"));
        assertThat(SessionService.hash("secret")).isNotEqualTo("secret");
    }

    private static AppUser user(UserRole role) throws Exception {
        AppUser user=new AppUser("Test",UUID.randomUUID()+"@example.com","hash",role,UserStatus.ACTIVE,Instant.now());
        Field id=AppUser.class.getDeclaredField("id");id.setAccessible(true);id.set(user,UUID.randomUUID());return user;
    }
}

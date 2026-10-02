package com.fivesense.api.auth.app;

import com.fivesense.api.auth.domain.*;
import com.fivesense.api.users.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;
import java.time.Instant;
import java.lang.reflect.Field;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtTokenServiceTests {
    @Test void issueAppliesRoleSpecificAccessLifetimeAndReturnsRefreshToken(){JwtEncoder encoder=mock(JwtEncoder.class);when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(Jwt.withTokenValue("signed").header("alg","RS256").claim("sub","test-user").claim("iat",Instant.now()).claim("exp",Instant.now().plusSeconds(3600)).build());JwtTokenService service=new JwtTokenService(encoder);Instant now=Instant.now();
        var manager=service.issue(user(UserRole.MANAGER),session(SessionType.ACCESS_ONLY,now),null,false,now);assertThat(manager.expiresIn()).isEqualTo(3600);assertThat(manager.accessToken()).isEqualTo("signed");
        var viewer=service.issue(user(UserRole.VIEWER),session(SessionType.REFRESHABLE,now),"refresh",false,now);assertThat(viewer.expiresIn()).isEqualTo(900);assertThat(viewer.refreshToken()).isEqualTo("refresh");
        var challenge=service.issue(user(UserRole.ADMIN),session(SessionType.PASSWORD_CHANGE,now),null,true,now);assertThat(challenge.expiresIn()).isEqualTo(300);assertThat(challenge.passwordChangeRequired()).isTrue();verify(encoder,times(3)).encode(any(JwtEncoderParameters.class));
    }
    private static AppUser user(UserRole role){AppUser user=new AppUser("Test","test@example.com","hash",role,UserStatus.ACTIVE,Instant.now());try{Field id=AppUser.class.getDeclaredField("id");id.setAccessible(true);id.set(user,UUID.randomUUID());}catch(ReflectiveOperationException e){throw new AssertionError(e);}return user;}
    private static AuthSession session(SessionType type,Instant now){return new AuthSession(UUID.randomUUID(),UUID.randomUUID(),null,type,now,now.plusSeconds(3600));}
}

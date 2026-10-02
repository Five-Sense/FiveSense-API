package com.fivesense.api.auth.app;

import com.fivesense.api.auth.domain.AuthSession;
import com.fivesense.api.auth.dto.AuthDtos;
import com.fivesense.api.users.domain.AppUser;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder;
    public JwtTokenService(JwtEncoder encoder){this.encoder=encoder;}
    public AuthDtos.TokenResponse issue(AppUser user,AuthSession session,String refreshToken,boolean passwordChange,Instant now){
        long ttl=passwordChange?300:user.getRole().name().equals("VIEWER")?900:3600;
        Instant expiry=now.plusSeconds(ttl);
        JwtClaimsSet claims=JwtClaimsSet.builder().issuer("five-sense-api").subject(user.getId().toString()).issuedAt(now).expiresAt(expiry).id(UUID.randomUUID().toString())
                .claim("role",user.getRole().name()).claim("sid",session.getId().toString()).claim("auth_version",user.getAuthVersion()).claim("pwd_change",passwordChange).build();
        JwsHeader header=JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build();
        String access=encoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
        return new AuthDtos.TokenResponse(access,refreshToken,"Bearer",ttl,passwordChange);
    }
}

package com.fivesense.api.auth.app;

import com.fivesense.api.auth.domain.*;
import com.fivesense.api.auth.infra.AuthSessionRepository;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.AppUser;
import com.fivesense.api.users.infra.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class SessionService {
    private final AuthSessionRepository sessions; private final UserRepository users; private final SecureRandom random=new SecureRandom();
    public SessionService(AuthSessionRepository sessions,UserRepository users){this.sessions=sessions;this.users=users;}
    @Transactional
    public SessionIssue create(AppUser user,Instant now,boolean passwordChange){
        SessionType type=passwordChange?SessionType.PASSWORD_CHANGE:user.getRole().name().equals("VIEWER")?SessionType.REFRESHABLE:SessionType.ACCESS_ONLY;
        if(type==SessionType.REFRESHABLE){
            var active=new ArrayList<>(sessions.findByUserIdAndSessionTypeAndRevokedAtIsNullOrderByCreatedAtAsc(user.getId(),SessionType.REFRESHABLE)
                    .stream().filter(s->s.getExpiresAt().isAfter(now)&&!s.getLastUsedAt().plus(Duration.ofHours(8)).isBefore(now)).toList());
            while(active.size()>=5){active.get(0).revoke(now);active.remove(0);}
        }
        long absolute=type==SessionType.REFRESHABLE?Duration.ofDays(30).toSeconds():passwordChange?300:3600;
        String raw=type==SessionType.REFRESHABLE?randomToken(UUID.randomUUID()):null;
        UUID id=raw==null?UUID.randomUUID():UUID.fromString(raw.substring(0,raw.indexOf('.')));
        AuthSession session=new AuthSession(id,user.getId(),raw==null?null:hash(raw),type,now,now.plusSeconds(absolute));
        sessions.save(session);
        return new SessionIssue(session,raw);
    }
    @Transactional(noRollbackFor = ApiException.class)
    public SessionIssue rotate(String raw,Instant now){
        UUID id; try{id=UUID.fromString(raw.substring(0,raw.indexOf('.')));}catch(RuntimeException ex){throw ApiException.badRequest("Invalid refresh token");}
        AuthSession session=sessions.findById(id).orElseThrow(()->ApiException.badRequest("Invalid refresh token"));
        if(session.getRevokedAt()!=null||session.getSessionType()!=SessionType.REFRESHABLE||!session.getExpiresAt().isAfter(now)||session.getLastUsedAt().plus(Duration.ofHours(8)).isBefore(now)) {session.revoke(now);throw ApiException.badRequest("Session expired");}
        if(!MessageDigest.isEqual(session.getRefreshTokenHash().getBytes(StandardCharsets.UTF_8),hash(raw).getBytes(StandardCharsets.UTF_8))){session.revoke(now);throw ApiException.badRequest("Refresh token reuse detected");}
        AppUser user=users.findById(session.getUserId()).orElseThrow(()->ApiException.badRequest("Invalid session"));
        String next=randomToken(session.getId());session.rotate(hash(next),now);return new SessionIssue(session,next);
    }
    @Transactional
    public void revoke(UUID sessionId,UUID userId,Instant now){sessions.findOwned(sessionId,userId).ifPresent(s->s.revoke(now));}
    @Transactional
    public void revokeAll(UUID userId,Instant now){sessions.revokeAll(userId,now);}
    public boolean isActive(UUID sessionId,UUID userId,Instant now){return sessions.findOwned(sessionId,userId).filter(s->s.getRevokedAt()==null&&s.getExpiresAt().isAfter(now)&&!s.getLastUsedAt().plus(Duration.ofHours(8)).isBefore(now)).isPresent();}
    private String randomToken(UUID sessionId){byte[] b=new byte[48];random.nextBytes(b);return sessionId+"."+Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
    public static String hash(String raw){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
    public record SessionIssue(AuthSession session,String refreshToken){}
}

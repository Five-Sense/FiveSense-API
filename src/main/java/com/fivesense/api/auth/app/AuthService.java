package com.fivesense.api.auth.app;

import com.fivesense.api.auth.domain.PasswordResetToken;
import com.fivesense.api.auth.dto.AuthDtos;
import com.fivesense.api.auth.infra.PasswordResetTokenRepository;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.shared.app.AfterCommit;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.infra.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern PASSWORD=Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,255}$");
    private final UserRepository users; private final PasswordResetTokenRepository resetTokens; private final PasswordEncoder passwords;
    private final SessionService sessions; private final JwtTokenService jwt; private final EmailService email; private final SecureRandom random=new SecureRandom();
    public AuthService(UserRepository users,PasswordResetTokenRepository resetTokens,PasswordEncoder passwords,SessionService sessions,JwtTokenService jwt,EmailService email){this.users=users;this.resetTokens=resetTokens;this.passwords=passwords;this.sessions=sessions;this.jwt=jwt;this.email=email;}

    @Transactional
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request){
        AppUser user=users.findByEmailIgnoreCase(request.email().trim().toLowerCase(Locale.ROOT)).orElseThrow(()->ApiException.badRequest("Invalid credentials"));
        if(user.getStatus()==UserStatus.INACTIVE||!passwords.matches(request.password(),user.getPasswordHash()))throw ApiException.badRequest("Invalid credentials");
        boolean change=user.getStatus()==UserStatus.FIRST_ACCESS;Instant now=Instant.now();var issue=sessions.create(user,now,change);
        return jwt.issue(user,issue.session(),issue.refreshToken(),change,now);
    }

    public AuthDtos.TokenResponse refresh(String refreshToken){
        Instant now=Instant.now();var issue=sessions.rotate(refreshToken,now);
        AppUser user=users.findById(issue.session().getUserId()).orElseThrow(()->ApiException.badRequest("Invalid session"));
        if(user.getStatus()!=UserStatus.ACTIVE||user.getRole()!=UserRole.VIEWER){sessions.revoke(issue.session().getId(),user.getId(),now);throw ApiException.badRequest("Session is no longer active");}
        return jwt.issue(user,issue.session(),issue.refreshToken(),false,now);
    }

    @Transactional
    public AuthDtos.TokenResponse changePassword(UUID userId,UUID sessionId,AuthDtos.ChangePasswordRequest request){
        AppUser user=users.findById(userId).orElseThrow(()->ApiException.notFound("User"));
        if(user.getStatus()==UserStatus.INACTIVE)throw ApiException.forbidden();
        boolean firstAccess=user.getStatus()==UserStatus.FIRST_ACCESS;
        if(!firstAccess&&(request.currentPassword()==null||!passwords.matches(request.currentPassword(),user.getPasswordHash())))throw ApiException.badRequest("Current password is invalid");
        validateNewPassword(request.newPassword(),request.confirmation());
        Instant now=Instant.now();user.changePassword(passwords.encode(request.newPassword()),UserStatus.ACTIVE,now);sessions.revokeAll(userId,now);
        var issue=sessions.create(user,now,false);
        return jwt.issue(user,issue.session(),issue.refreshToken(),false,now);
    }

    @Transactional
    public void logout(UUID userId,UUID sessionId){sessions.revoke(sessionId,userId,Instant.now());}

    @Transactional
    public AuthDtos.MessageResponse requestPasswordReset(String emailAddress){
        users.findByEmailIgnoreCase(emailAddress.trim().toLowerCase(Locale.ROOT)).filter(u->u.getStatus()==UserStatus.ACTIVE).ifPresent(user->{
            String raw=randomToken();Instant now=Instant.now();resetTokens.save(new PasswordResetToken(user.getId(),SessionService.hash(raw),now.plus(Duration.ofMinutes(10)),now));
            String recipient=user.getEmail();AfterCommit.run(()->email.send(recipient,"Recuperação de senha Five Sense","Use este token de recuperação em até 10 minutos: "+raw));
        });
        return new AuthDtos.MessageResponse("If the account is eligible, a reset message will be sent");
    }

    @Transactional
    public void confirmPasswordReset(AuthDtos.ResetConfirmRequest request){
        validateNewPassword(request.newPassword(),request.confirmation());
        Instant now=Instant.now();PasswordResetToken token=resetTokens.findByTokenHash(SessionService.hash(request.token())).orElseThrow(()->ApiException.badRequest("Invalid or expired reset token"));
        if(token.getUsedAt()!=null||!token.getExpiresAt().isAfter(now))throw ApiException.badRequest("Invalid or expired reset token");
        AppUser user=users.findById(token.getUserId()).orElseThrow(()->ApiException.badRequest("Invalid or expired reset token"));
        user.changePassword(passwords.encode(request.newPassword()),UserStatus.ACTIVE,now);token.markUsed(now);sessions.revokeAll(user.getId(),now);
    }

    private static void validateNewPassword(String password,String confirmation){
        if(!Objects.equals(password,confirmation))throw ApiException.badRequest("Password confirmation does not match");
        if(!PASSWORD.matcher(password).matches())throw ApiException.badRequest("Password must have at least 8 characters, one uppercase letter, one number and one special character");
    }
    private String randomToken(){byte[] value=new byte[32];random.nextBytes(value);return Base64.getUrlEncoder().withoutPadding().encodeToString(value);}
}

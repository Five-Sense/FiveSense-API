package com.fivesense.api.users.app;

import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.shared.app.AfterCommit;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.dto.UserDtos;
import com.fivesense.api.users.infra.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
public class UserService {
    private final UserRepository users; private final UserMapper mapper; private final PasswordEncoder passwords; private final EmailService email;
    private final SecureRandom random=new SecureRandom();
    public UserService(UserRepository users,UserMapper mapper,PasswordEncoder passwords,EmailService email){this.users=users;this.mapper=mapper;this.passwords=passwords;this.email=email;}

    @Transactional
    public UserDtos.Response create(UUID actorId,UserDtos.CreateRequest request){
        AppUser actor=users.findById(actorId).orElseThrow(()->ApiException.forbidden());
        boolean allowed=request.role()!=UserRole.ADMIN&&(actor.getRole()==UserRole.ADMIN || actor.getRole()==UserRole.MANAGER&&request.role()==UserRole.VIEWER);
        if(!allowed)throw ApiException.forbidden();
        String address=normalize(request.email());
        if(users.existsByEmailIgnoreCase(address))throw ApiException.conflict("Email already exists");
        Instant now=Instant.now();String temporary=randomPassword();
        AppUser created=users.save(new AppUser(request.name().trim(),address,passwords.encode(temporary),request.role(),UserStatus.FIRST_ACCESS,now));
        created.recordCredentialEmail(now);
        AfterCommit.run(()->email.send(address,"Acesso inicial Five Sense","Sua senha temporária é: "+temporary+"\nNo primeiro acesso, altere a senha para continuar."));
        return mapper.toResponse(created);
    }

    @Transactional(readOnly=true)
    public Page<UserDtos.Response> list(UUID actorId,Pageable pageable){
        AppUser actor=users.findById(actorId).orElseThrow(()->ApiException.forbidden());
        Page<AppUser> page=actor.getRole()==UserRole.ADMIN?users.findAll(pageable):users.findByRoleIn(List.of(UserRole.VIEWER),pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly=true)
    public UserDtos.Response get(UUID actorId,UUID targetId){
        AppUser actor=users.findById(actorId).orElseThrow(()->ApiException.forbidden());
        AppUser target=users.findById(targetId).orElseThrow(()->ApiException.notFound("User"));
        if(actor.getRole()!=UserRole.ADMIN&&(actor.getRole()!=UserRole.MANAGER||target.getRole()!=UserRole.VIEWER))throw ApiException.forbidden();
        return mapper.toResponse(target);
    }

    @Transactional
    public UserDtos.Response update(UUID actorId,UUID targetId,UserDtos.UpdateRequest request){
        AppUser actor=users.findById(actorId).orElseThrow(()->ApiException.forbidden());
        AppUser target=users.findById(targetId).orElseThrow(()->ApiException.notFound("User"));
        if(actor.getRole()!=UserRole.ADMIN&&(actor.getRole()!=UserRole.MANAGER||target.getRole()!=UserRole.VIEWER))throw ApiException.forbidden();
        if(target.getStatus()==UserStatus.FIRST_ACCESS)throw ApiException.conflict("Complete the initial password change before editing account status");
        String address=normalize(request.email());
        users.findByEmailIgnoreCase(address).filter(u->!u.getId().equals(targetId)).ifPresent(u->{throw ApiException.conflict("Email already exists");});
        if(request.status()==UserStatus.FIRST_ACCESS)throw ApiException.badRequest("FIRST_ACCESS is controlled by credential lifecycle");
        target.update(request.name().trim(),address,target.getRole(),request.status(),Instant.now());
        return mapper.toResponse(target);
    }

    @Transactional
    public void resendInitialPassword(UUID actorId,UUID targetId){
        AppUser actor=users.findById(actorId).orElseThrow(()->ApiException.forbidden());
        AppUser target=users.findById(targetId).orElseThrow(()->ApiException.notFound("User"));
        if(actor.getRole()!=UserRole.ADMIN&&(actor.getRole()!=UserRole.MANAGER||target.getRole()!=UserRole.VIEWER))throw ApiException.forbidden();
        if(target.getStatus()!=UserStatus.FIRST_ACCESS)throw ApiException.conflict("Account has already completed first access");
        Instant now=Instant.now();
        if(!target.canResendCredentialEmail(now))throw ApiException.conflict("Initial password resend limit reached");
        String temporary=randomPassword();target.changePassword(passwords.encode(temporary),UserStatus.FIRST_ACCESS,now);target.recordCredentialEmail(now);
        String targetEmail=target.getEmail();
        AfterCommit.run(()->email.send(targetEmail,"Nova senha temporária Five Sense","Sua nova senha temporária é: "+temporary+"\nA senha anterior foi invalidada. Altere a senha no primeiro acesso."));
    }

    @Transactional(readOnly=true)
    public List<String> notificationRecipients(){return users.findByRoleInAndStatus(List.of(UserRole.ADMIN,UserRole.MANAGER),UserStatus.ACTIVE).stream().map(AppUser::getEmail).toList();}

    private static String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
    private String randomPassword(){
        String upper="ABCDEFGHJKLMNPQRSTUVWXYZ", lower="abcdefghijkmnopqrstuvwxyz", digits="23456789", symbols="!@#$%&*+-_";
        String all=upper+lower+digits+symbols;StringBuilder result=new StringBuilder();
        result.append(upper.charAt(random.nextInt(upper.length()))).append(digits.charAt(random.nextInt(digits.length()))).append(symbols.charAt(random.nextInt(symbols.length())));
        while(result.length()<20)result.append(all.charAt(random.nextInt(all.length())));
        char[] chars=result.toString().toCharArray();for(int i=chars.length-1;i>0;i--){int j=random.nextInt(i+1);char t=chars[i];chars[i]=chars[j];chars[j]=t;}
        return new String(chars);
    }
}

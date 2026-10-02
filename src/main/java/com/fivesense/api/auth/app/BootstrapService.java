package com.fivesense.api.auth.app;

import com.fivesense.api.auth.domain.BootstrapState;
import com.fivesense.api.auth.infra.BootstrapStateRepository;
import com.fivesense.api.auth.dto.AuthDtos;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.infra.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
public class BootstrapService {
    private final BootstrapStateRepository state;
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final SecureRandom random=new SecureRandom();
    private final String secret;
    private final String initialAdminEmail;
    private final String initialAdminName;

    public BootstrapService(BootstrapStateRepository state,UserRepository users,PasswordEncoder passwords,
            @Value("${security.bootstrap.secret:}") String secret,
            @Value("${security.bootstrap.initial-admin.email:}") String initialAdminEmail,
            @Value("${security.bootstrap.initial-admin.name:}") String initialAdminName){
        this.state=state;this.users=users;this.passwords=passwords;this.secret=secret;
        this.initialAdminEmail=initialAdminEmail;this.initialAdminName=initialAdminName;
    }

    @Transactional
    public void prepareInitialAdmin(){
        BootstrapState bootstrap=state.findById(1).orElseThrow(()->new IllegalStateException("Bootstrap state not initialized"));
        if(bootstrap.isConsumed()||bootstrap.getInitialPasswordCiphertext()!=null)return;
        if(secret.length()<32)throw new IllegalStateException("Bootstrap secret must contain at least 32 characters before initial provisioning");
        if(initialAdminEmail.isBlank()||initialAdminName.isBlank())throw new IllegalStateException("Initial administrator email and name must be configured");
        if(users.existsByRole(UserRole.ADMIN))throw new IllegalStateException("An administrator exists but initial bootstrap has not been consumed");
        String email=initialAdminEmail.trim().toLowerCase(Locale.ROOT);
        if(users.existsByEmailIgnoreCase(email))throw new IllegalStateException("Initial administrator email already belongs to another account");
        String initialPassword=randomPassword();Instant now=Instant.now();
        users.save(new AppUser(initialAdminName.trim(),email,passwords.encode(initialPassword),UserRole.ADMIN,UserStatus.FIRST_ACCESS,now));
        bootstrap.setInitialPasswordCiphertext(encrypt(initialPassword));
    }

    @Transactional
    public AuthDtos.BootstrapResponse revealInitialAdminPassword(String suppliedSecret){
        if(secret.length()<32||suppliedSecret==null||!MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8),suppliedSecret.getBytes(StandardCharsets.UTF_8)))
            throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN,"Bootstrap authorization failed");
        BootstrapState bootstrap=state.findById(1).orElseThrow(()->new IllegalStateException("Bootstrap state not initialized"));
        if(bootstrap.isConsumed()||bootstrap.getInitialPasswordCiphertext()==null)throw ApiException.conflict("Initial administrator password was already revealed");
        AppUser admin=users.findByRoleIn(List.of(UserRole.ADMIN)).stream().findFirst().orElseThrow(()->new IllegalStateException("Initial administrator was not provisioned"));
        String initialPassword=decrypt(bootstrap.getInitialPasswordCiphertext());
        bootstrap.consume();
        return new AuthDtos.BootstrapResponse(admin.getEmail(),initialPassword);
    }

    private String encrypt(String plaintext){
        try{
            byte[] nonce=new byte[12];random.nextBytes(nonce);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,key(),new GCMParameterSpec(128,nonce));
            byte[] encrypted=cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length+encrypted.length).put(nonce).put(encrypted).array());
        }catch(GeneralSecurityException ex){throw new IllegalStateException("Could not protect initial administrator credential",ex);}
    }

    private String decrypt(String ciphertext){
        try{
            byte[] packed=Base64.getDecoder().decode(ciphertext);ByteBuffer buffer=ByteBuffer.wrap(packed);byte[] nonce=new byte[12];buffer.get(nonce);byte[] encrypted=new byte[buffer.remaining()];buffer.get(encrypted);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,nonce));
            return new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8);
        }catch(GeneralSecurityException|IllegalArgumentException ex){throw new IllegalStateException("Could not reveal initial administrator credential",ex);}
    }

    private SecretKeySpec key(){
        try{return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)),"AES");}
        catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
    }

    private String randomPassword(){
        String upper="ABCDEFGHJKLMNPQRSTUVWXYZ",lower="abcdefghijkmnopqrstuvwxyz",digits="23456789",symbols="!@#$%&*+-_",all=upper+lower+digits+symbols;
        StringBuilder value=new StringBuilder();value.append(upper.charAt(random.nextInt(upper.length()))).append(digits.charAt(random.nextInt(digits.length()))).append(symbols.charAt(random.nextInt(symbols.length())));
        while(value.length()<24)value.append(all.charAt(random.nextInt(all.length())));
        char[] chars=value.toString().toCharArray();for(int i=chars.length-1;i>0;i--){int j=random.nextInt(i+1);char t=chars[i];chars[i]=chars[j];chars[j]=t;}
        return new String(chars);
    }
}

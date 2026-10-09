package com.fivesense.api.auth.app;

import com.fivesense.api.auth.dto.AuthDtos;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.infra.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class AuthService {
    private final UserRepository users;
    public AuthService(UserRepository users){this.users=users;}

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request){
        AppUser user=users.findByUsernameIgnoreCase(request.username().trim().toLowerCase(Locale.ROOT)).orElseThrow(()->ApiException.badRequest("Invalid credentials"));
        if(user.getStatus()==UserStatus.INACTIVE||!request.password().equals(user.getPassword()))throw ApiException.badRequest("Invalid credentials");
        return new AuthDtos.LoginResponse(true,user.getId(),user.getName(),user.getUsername(),user.getEmail(),user.getRole());
    }

    @Transactional
    public AuthDtos.ForgotPasswordResponse forgotPassword(AuthDtos.ForgotPasswordRequest request){
        AppUser user=users.findByEmailIgnoreCase(request.email().trim().toLowerCase(Locale.ROOT)).orElseThrow(()->ApiException.notFound("User"));
        user.changePassword(request.newPassword(),Instant.now());
        return new AuthDtos.ForgotPasswordResponse(true,user.getId(),user.getEmail());
    }
}

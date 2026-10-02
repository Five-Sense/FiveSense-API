package com.fivesense.api.auth.controller;

import com.fivesense.api.auth.app.AuthService;
import com.fivesense.api.auth.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}
    @PostMapping("/login") public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request){return service.login(request);}
    @PostMapping("/refresh") public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request){return service.refresh(request.refreshToken());}
    @PostMapping("/change-password") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER','PASSWORD_CHANGE')")
    public AuthDtos.TokenResponse change(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody AuthDtos.ChangePasswordRequest request){return service.changePassword(userId(jwt),UUID.fromString(jwt.getClaimAsString("sid")),request);}
    @PostMapping("/logout") @PreAuthorize("isAuthenticated()")
    public void logout(@AuthenticationPrincipal Jwt jwt){service.logout(userId(jwt),UUID.fromString(jwt.getClaimAsString("sid")));}
    @PostMapping("/password-reset/request") public AuthDtos.MessageResponse resetRequest(@Valid @RequestBody AuthDtos.ResetRequest request){return service.requestPasswordReset(request.email());}
    @PostMapping("/password-reset/confirm") public AuthDtos.MessageResponse resetConfirm(@Valid @RequestBody AuthDtos.ResetConfirmRequest request){service.confirmPasswordReset(request);return new AuthDtos.MessageResponse("Password updated");}
    private static UUID userId(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}

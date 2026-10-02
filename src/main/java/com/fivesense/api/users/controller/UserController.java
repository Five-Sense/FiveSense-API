package com.fivesense.api.users.controller;

import com.fivesense.api.users.app.UserService;
import com.fivesense.api.users.dto.UserDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service){this.service=service;}
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Page<UserDtos.Response> list(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(userId(jwt),PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("createdAt").descending()));}
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserDtos.Response get(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.get(userId(jwt),id);}
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserDtos.Response create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody UserDtos.CreateRequest request){return service.create(userId(jwt),request);}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserDtos.Response update(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody UserDtos.UpdateRequest request){return service.update(userId(jwt),id,request);}
    @PostMapping("/{id}/resend-initial-password") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void resend(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){service.resendInitialPassword(userId(jwt),id);}
    private static UUID userId(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}

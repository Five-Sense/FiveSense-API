package com.fivesense.api.auth.controller;

import com.fivesense.api.auth.app.BootstrapService;
import com.fivesense.api.auth.dto.AuthDtos;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/bootstrap")
public class BootstrapController {
    private final BootstrapService service;public BootstrapController(BootstrapService service){this.service=service;}
    @PostMapping("/admin")
    public AuthDtos.BootstrapResponse revealInitialAdminPassword(@RequestHeader(name="X-Bootstrap-Secret",required=false) String secret){return service.revealInitialAdminPassword(secret);}
}

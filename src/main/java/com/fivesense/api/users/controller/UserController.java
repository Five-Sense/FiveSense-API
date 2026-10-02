package com.fivesense.api.users.controller;

import com.fivesense.api.users.app.UserService;
import com.fivesense.api.users.dto.UserDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service){this.service=service;}

    @GetMapping
    public Page<UserDtos.Response> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("createdAt").descending()));}

    @GetMapping("/{id}")
    public UserDtos.Response get(@PathVariable UUID id){return service.get(id);}

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public UserDtos.Response create(@Valid @RequestBody UserDtos.CreateRequest request){return service.create(request);}

    @PutMapping("/{id}")
    public UserDtos.Response update(@PathVariable UUID id,@Valid @RequestBody UserDtos.UpdateRequest request){return service.update(id,request);}
}

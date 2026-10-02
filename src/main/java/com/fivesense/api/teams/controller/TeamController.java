package com.fivesense.api.teams.controller;

import com.fivesense.api.teams.app.TeamService;
import com.fivesense.api.teams.dto.TeamDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/teams")
public class TeamController {
    private final TeamService service;public TeamController(TeamService service){this.service=service;}
    @GetMapping
    public Page<TeamDtos.Response> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("name").ascending()));}
    @GetMapping("/{id}") public TeamDtos.Response get(@PathVariable UUID id){return service.get(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public TeamDtos.Response create(@Valid @RequestBody TeamDtos.UpsertRequest request){return service.create(request);}
    @PutMapping("/{id}") public TeamDtos.Response update(@PathVariable UUID id,@Valid @RequestBody TeamDtos.UpsertRequest request){return service.update(id,request);}
    @PatchMapping("/{id}/status") public TeamDtos.Response updateStatus(@PathVariable UUID id,@Valid @RequestBody TeamDtos.StatusRequest request){return service.updateStatus(id,request.status());}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id){service.delete(id);}
}

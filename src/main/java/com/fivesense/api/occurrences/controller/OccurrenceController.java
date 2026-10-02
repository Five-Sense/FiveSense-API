package com.fivesense.api.occurrences.controller;

import com.fivesense.api.occurrences.app.OccurrenceService;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/occurrences")
public class OccurrenceController {
    private final OccurrenceService service;public OccurrenceController(OccurrenceService service){this.service=service;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public OccurrenceDtos.Response create(@Valid @RequestBody OccurrenceDtos.CreateRequest request){return service.create(request.reportedByUserId(),request);}
    @GetMapping public Page<OccurrenceDtos.Response> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("createdAt").descending()));}
}

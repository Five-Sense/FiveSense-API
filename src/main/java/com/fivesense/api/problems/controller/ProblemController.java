package com.fivesense.api.problems.controller;

import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.problems.dto.ProblemDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/problems")
public class ProblemController {
    private final ProblemService service;public ProblemController(ProblemService service){this.service=service;}
    @GetMapping public Page<ProblemDtos.Response> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("name").ascending()));}
    @GetMapping("/options") public List<ProblemDtos.Option> options(){return service.optionsForOccurrence();}
    @GetMapping("/{id}") public ProblemDtos.Response get(@PathVariable UUID id){return service.get(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ProblemDtos.Response create(@Valid @RequestBody ProblemDtos.UpsertRequest request){return service.create(request);}
    @PutMapping("/{id}") public ProblemDtos.Response update(@PathVariable UUID id,@Valid @RequestBody ProblemDtos.UpsertRequest request){return service.update(id,request);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id){service.delete(id);}
}

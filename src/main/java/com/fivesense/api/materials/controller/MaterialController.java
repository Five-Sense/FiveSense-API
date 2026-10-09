package com.fivesense.api.materials.controller;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.materials.dto.MaterialDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/materials")
public class MaterialController {
    private final MaterialService service;public MaterialController(MaterialService service){this.service=service;}
    @GetMapping public Page<MaterialDtos.Response> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("name").ascending()));}
    @GetMapping("/stock") public List<MaterialDtos.StockView> stock(){return service.stock();}
    @GetMapping("/options") public List<MaterialDtos.Option> options(){return service.optionsForOccurrence();}
    @GetMapping("/{id}") public MaterialDtos.Response get(@PathVariable UUID id){return service.get(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MaterialDtos.Response create(@Valid @RequestBody MaterialDtos.UpsertRequest request){return service.create(request);}
    @PutMapping("/{id}") public MaterialDtos.Response update(@PathVariable UUID id,@Valid @RequestBody MaterialDtos.UpsertRequest request){return service.update(id,request);}
    @PatchMapping("/{id}/stock") public MaterialDtos.Response adjustStock(@PathVariable UUID id,@Valid @RequestBody MaterialDtos.StockAdjustRequest request){return service.adjustStock(id,request.delta());}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id){service.delete(id);}
}

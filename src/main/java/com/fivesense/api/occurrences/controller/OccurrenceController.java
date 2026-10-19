package com.fivesense.api.occurrences.controller;

import com.fivesense.api.occurrences.app.OccurrenceService;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController @RequestMapping("/api/v1/occurrences")
public class OccurrenceController {
    private static final String IMAGES_FIELD="images";
    private final OccurrenceService service;public OccurrenceController(OccurrenceService service){this.service=service;}

    /** JSON variant: optional {@code images} items carry each image as base64 text. */
    @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public OccurrenceDtos.Response create(@Valid @RequestBody OccurrenceDtos.CreateRequest request){return service.create(request,null,null);}

    /** Multipart variant: the text fields plus any number of {@code images} parts, each a binary file or a base64 text value. */
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public OccurrenceDtos.Response createWithImages(@Valid @ModelAttribute OccurrenceDtos.CreateForm form,@RequestParam MultiValueMap<String,MultipartFile> files,@RequestParam MultiValueMap<String,String> fields){
        List<MultipartFile> uploaded=files.get(IMAGES_FIELD);List<String> encoded=fields.get(IMAGES_FIELD);
        return service.create(form.toRequest(),uploaded,encoded);
    }

    @GetMapping public Page<OccurrenceDtos.Response> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("createdAt").descending()));}
}

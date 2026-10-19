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
import java.util.ArrayList;
import java.util.List;

@RestController @RequestMapping("/api/v1/occurrences")
public class OccurrenceController {
    private static final List<String> IMAGE_FIELDS=List.of("images","image");
    private final OccurrenceService service;public OccurrenceController(OccurrenceService service){this.service=service;}

    /**
     * Multipart upload of one or more images (up to {@link OccurrenceDtos#MAX_IMAGES}). Each value of the
     * {@code images} (or legacy {@code image}) field can be a file part or a text part with the image in base64.
     */
    @PostMapping(path="/images",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public OccurrenceDtos.ImageUploadResponse uploadImages(@RequestParam MultiValueMap<String,MultipartFile> files,@RequestParam MultiValueMap<String,String> fields){
        return service.storeImages(valuesOf(files),valuesOf(fields));
    }

    /** JSON upload of one or more images sent as base64 text. */
    @PostMapping(path="/images",consumes=MediaType.APPLICATION_JSON_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public OccurrenceDtos.ImageUploadResponse uploadEncodedImages(@Valid @RequestBody OccurrenceDtos.ImageBatchRequest request){
        return service.storeImages(request);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) public OccurrenceDtos.Response create(@Valid @RequestBody OccurrenceDtos.CreateRequest request){return service.create(request.reportedByUserId(),request);}
    @GetMapping public Page<OccurrenceDtos.Response> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("createdAt").descending()));}

    private static <T> List<T> valuesOf(MultiValueMap<String,T> parts){
        List<T> values=new ArrayList<>();
        IMAGE_FIELDS.forEach(field->{List<T> found=parts.get(field);if(found!=null)values.addAll(found);});
        return values;
    }
}

package com.fivesense.api.occurrences.app;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.occurrences.infra.TemporaryImageStore;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.shared.app.AfterCommit;
import com.fivesense.api.users.app.UserService;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class OccurrenceService {
    private final OccurrenceRepository occurrences;private final OccurrenceMapper mapper;private final ProblemService problems;private final MaterialService materials;private final UserService users;private final EmailService email;private final TemporaryImageStore images;
    public OccurrenceService(OccurrenceRepository occurrences,OccurrenceMapper mapper,ProblemService problems,MaterialService materials,UserService users,EmailService email,TemporaryImageStore images){this.occurrences=occurrences;this.mapper=mapper;this.problems=problems;this.materials=materials;this.users=users;this.email=email;this.images=images;}

    /**
     * Stores uploaded images (multipart files and/or base64 text values) in temporary files — never in
     * the database — and returns their ids. All-or-nothing: if one image is rejected, the ones already
     * stored by this call are discarded.
     */
    public OccurrenceDtos.ImageUploadResponse storeImages(List<MultipartFile> files,List<String> encoded){
        List<Supplier<UUID>> tasks=new ArrayList<>();
        if(files!=null)files.forEach(file->tasks.add(()->storeFile(file)));
        if(encoded!=null)encoded.forEach(text->tasks.add(()->storeEncoded(null,text)));
        return storeAll(tasks);
    }

    /** JSON variant of {@link #storeImages(List,List)}: every image arrives as base64 text with an optional file name. */
    public OccurrenceDtos.ImageUploadResponse storeImages(OccurrenceDtos.ImageBatchRequest request){
        List<Supplier<UUID>> tasks=new ArrayList<>();
        request.images().forEach(image->tasks.add(()->storeEncoded(image.name(),image.data())));
        return storeAll(tasks);
    }

    @Transactional public OccurrenceDtos.Response create(UUID actorId,OccurrenceDtos.CreateRequest request){
        var problem=problems.requireActive(request.problemId());materials.requireActive(request.materialId());
        List<UUID> imageIds=imageIdsOf(request);
        Occurrence occurrence=occurrences.save(new Occurrence(problem.id(),request.materialId(),actorId,request.affectedQuantity(),Instant.now()));
        String subject="Nova ocorrência Five Sense: "+problem.name();String body="Uma ocorrência foi registrada.\nProblema: "+problem.name()+"\nMaterial ID: "+request.materialId()+"\nQuantidade afetada: "+request.affectedQuantity()+"\nOcorrência: "+occurrence.getId();
        var recipients=users.notificationRecipients();
        List<EmailService.Attachment> attachments=imageIds.stream().map(images::locate).flatMap(Optional::stream).map(image->new EmailService.Attachment(image.path(),image.originalName())).toList();
        AfterCommit.run(()->{
            try {
                if(attachments.isEmpty()){
                    recipients.forEach(address->email.send(address,subject,body));
                    email.send(problem.email(),"Ocorrência recebida: "+problem.name(),problem.defaultResponse());
                } else {
                    recipients.forEach(address->email.sendWithAttachments(address,subject,body,attachments));
                    email.sendWithAttachments(problem.email(),"Ocorrência recebida: "+problem.name(),problem.defaultResponse(),attachments);
                }
            } finally {
                imageIds.forEach(images::delete);
            }
        });
        return mapper.toResponse(occurrence);
    }
    @Transactional(readOnly=true) public Page<OccurrenceDtos.Response> list(Pageable pageable){return occurrences.findAll(pageable).map(mapper::toResponse);}

    private OccurrenceDtos.ImageUploadResponse storeAll(List<Supplier<UUID>> tasks){
        if(tasks.isEmpty())throw ApiException.badRequest("At least one image is required");
        if(tasks.size()>OccurrenceDtos.MAX_IMAGES)throw ApiException.badRequest("At most "+OccurrenceDtos.MAX_IMAGES+" images are allowed");
        List<UUID> stored=new ArrayList<>();
        try { tasks.forEach(task->stored.add(task.get())); }
        catch (RuntimeException ex){ stored.forEach(images::delete); throw ex; }
        return OccurrenceDtos.ImageUploadResponse.of(stored);
    }

    private UUID storeFile(MultipartFile file){
        try { return images.store(file.getInputStream(),file.getOriginalFilename()); }
        catch (IOException ex){ throw ApiException.badRequest("Unable to read the uploaded image"); }
    }

    private UUID storeEncoded(String name,String data){
        return images.store(new ByteArrayInputStream(decode(data)),name);
    }

    /** Accepts plain base64 (standard or URL-safe) or a {@code data:...;base64,} URI; whitespace and line breaks are ignored. */
    private byte[] decode(String data){
        String payload=data==null?"":data.strip();
        if(payload.startsWith("data:")){
            int comma=payload.indexOf(',');
            if(comma<0)throw ApiException.badRequest("Image content is not valid base64");
            payload=payload.substring(comma+1);
        }
        payload=payload.replaceAll("\\s","");
        if(payload.length()>images.maxBytes()/3*4+8)throw new ApiException(HttpStatus.CONTENT_TOO_LARGE,"Image exceeds the maximum allowed size");
        try { return (payload.indexOf('-')>=0||payload.indexOf('_')>=0?Base64.getUrlDecoder():Base64.getDecoder()).decode(payload); }
        catch (IllegalArgumentException ex){ throw ApiException.badRequest("Image content is not valid base64"); }
    }

    /** Merges the legacy single {@code imageId} with {@code imageIds}, keeping order and dropping duplicates. */
    private static List<UUID> imageIdsOf(OccurrenceDtos.CreateRequest request){
        LinkedHashSet<UUID> ids=new LinkedHashSet<>();
        if(request.imageId()!=null)ids.add(request.imageId());
        if(request.imageIds()!=null)ids.addAll(request.imageIds());
        if(ids.size()>OccurrenceDtos.MAX_IMAGES)throw ApiException.badRequest("At most "+OccurrenceDtos.MAX_IMAGES+" images are allowed");
        return List.copyOf(ids);
    }
}

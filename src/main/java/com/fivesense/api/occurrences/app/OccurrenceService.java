package com.fivesense.api.occurrences.app;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.occurrences.infra.TemporaryImageStore;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.shared.app.AfterCommit;
import com.fivesense.api.users.app.UserService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class OccurrenceService {
    private final OccurrenceRepository occurrences;private final OccurrenceMapper mapper;private final ProblemService problems;private final MaterialService materials;private final UserService users;private final EmailService email;private final TemporaryImageStore images;
    public OccurrenceService(OccurrenceRepository occurrences,OccurrenceMapper mapper,ProblemService problems,MaterialService materials,UserService users,EmailService email,TemporaryImageStore images){this.occurrences=occurrences;this.mapper=mapper;this.problems=problems;this.materials=materials;this.users=users;this.email=email;this.images=images;}

    /** Stores an uploaded image in a temporary file (never in the database) and returns its id. */
    public UUID storeImage(MultipartFile file){
        try { return images.store(file.getInputStream(),file.getOriginalFilename()); }
        catch (IOException ex){ throw com.fivesense.api.shared.error.ApiException.badRequest("Unable to read the uploaded image"); }
    }

    @Transactional public OccurrenceDtos.Response create(UUID actorId,OccurrenceDtos.CreateRequest request){
        var problem=problems.requireActive(request.problemId());materials.requireActive(request.materialId());
        Occurrence occurrence=occurrences.save(new Occurrence(problem.id(),request.materialId(),actorId,request.affectedQuantity(),Instant.now()));
        String subject="Nova ocorrência Five Sense: "+problem.name();String body="Uma ocorrência foi registrada.\nProblema: "+problem.name()+"\nMaterial ID: "+request.materialId()+"\nQuantidade afetada: "+request.affectedQuantity()+"\nOcorrência: "+occurrence.getId();
        var recipients=users.notificationRecipients();
        UUID imageId=request.imageId();
        var storedImage=images.locate(imageId);
        AfterCommit.run(()->{
            storedImage.ifPresentOrElse(
                image->{
                    recipients.forEach(address->email.sendWithAttachment(address,subject,body,image.path(),image.originalName()));
                    email.sendWithAttachment(problem.email(),"Ocorrência recebida: "+problem.name(),problem.defaultResponse(),image.path(),image.originalName());
                },
                ()->{
                    recipients.forEach(address->email.send(address,subject,body));
                    email.send(problem.email(),"Ocorrência recebida: "+problem.name(),problem.defaultResponse());
                });
            images.delete(imageId);
        });
        return mapper.toResponse(occurrence);
    }
    @Transactional(readOnly=true) public Page<OccurrenceDtos.Response> list(Pageable pageable){return occurrences.findAll(pageable).map(mapper::toResponse);}
}

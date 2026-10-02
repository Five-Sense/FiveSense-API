package com.fivesense.api.occurrences.app;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.shared.app.AfterCommit;
import com.fivesense.api.users.app.UserService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class OccurrenceService {
    private final OccurrenceRepository occurrences;private final OccurrenceMapper mapper;private final ProblemService problems;private final MaterialService materials;private final UserService users;private final EmailService email;
    public OccurrenceService(OccurrenceRepository occurrences,OccurrenceMapper mapper,ProblemService problems,MaterialService materials,UserService users,EmailService email){this.occurrences=occurrences;this.mapper=mapper;this.problems=problems;this.materials=materials;this.users=users;this.email=email;}
    @Transactional public OccurrenceDtos.Response create(UUID actorId,OccurrenceDtos.CreateRequest request){
        var problem=problems.requireActive(request.problemId());materials.requireActive(request.materialId());
        Occurrence occurrence=occurrences.save(new Occurrence(problem.id(),request.materialId(),actorId,request.affectedQuantity(),Instant.now()));
        String subject="Nova ocorrência Five Sense: "+problem.name();String body="Uma ocorrência foi registrada.\nProblema: "+problem.name()+"\nMaterial ID: "+request.materialId()+"\nQuantidade afetada: "+request.affectedQuantity()+"\nOcorrência: "+occurrence.getId();
        var recipients=users.notificationRecipients();AfterCommit.run(()->{recipients.forEach(address->email.send(address,subject,body));email.send(problem.email(),"Ocorrência recebida: "+problem.name(),problem.defaultResponse());});
        return mapper.toResponse(occurrence);
    }
    @Transactional(readOnly=true) public Page<OccurrenceDtos.Response> list(Pageable pageable){return occurrences.findAll(pageable).map(mapper::toResponse);}
}

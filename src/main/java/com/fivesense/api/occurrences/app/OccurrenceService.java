package com.fivesense.api.occurrences.app;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.shared.app.AfterCommit;
import com.fivesense.api.users.app.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OccurrenceService {
    private static final Logger log=LoggerFactory.getLogger(OccurrenceService.class);
    private static final String TEMPLATE="email/occurrence";
    private final OccurrenceRepository occurrences;private final OccurrenceMapper mapper;private final ProblemService problems;private final MaterialService materials;private final UserService users;private final EmailService email;private final OccurrenceImageReader imageReader;
    public OccurrenceService(OccurrenceRepository occurrences,OccurrenceMapper mapper,ProblemService problems,MaterialService materials,UserService users,EmailService email,OccurrenceImageReader imageReader){this.occurrences=occurrences;this.mapper=mapper;this.problems=problems;this.materials=materials;this.users=users;this.email=email;this.imageReader=imageReader;}

    /**
     * Registers the occurrence and, after the commit, queues the notification e-mails (sent on a separate
     * thread). Images come from the JSON {@code request.images()} and/or from multipart {@code files} and
     * base64 {@code encoded} parts; they are validated first, kept in memory only, embedded in the e-mails
     * and never stored.
     */
    @Transactional public OccurrenceDtos.Response create(OccurrenceDtos.CreateRequest request,List<MultipartFile> files,List<String> encoded){
        String reporter=users.requireActive(request.reportedByUserId());
        var problem=problems.requireActive(request.problemId());
        String material=materials.requireActive(request.materialId());
        Occurrence occurrence=occurrences.save(new Occurrence(problem.id(),request.materialId(),request.reportedByUserId(),request.affectedQuantity(),Instant.now()));
        var recipients=users.notificationRecipients();
        if(recipients.isEmpty())log.warn("No active ADMIN or MANAGER to notify about occurrence {}",occurrence.getId());

        // Images are only used to enrich the notification e-mail; they are never persisted and must never
        // block the occurrence from being saved. Any problem reading them is logged and the occurrence
        // still goes through, with the e-mail sent without the failed images.
        var images=readImagesBestEffort(files,encoded,request.images(),occurrence.getId());

        Map<String,String> details=new LinkedHashMap<>();
        details.put("Problema",problem.name());details.put("Material",material);details.put("Quantidade afetada",String.valueOf(request.affectedQuantity()));
        details.put("Registrada por",reporter);details.put("Ocorrência",String.valueOf(occurrence.getId()));
        String title="Nova ocorrência: "+problem.name();
        String intro="Uma ocorrência foi registrada no Five Sense.";
        var notification=new EmailService.HtmlMessage(null,title,plainText(title,intro,details,images.size()),TEMPLATE,model(title,intro,details),images);

        Map<String,String> contactDetails=new LinkedHashMap<>();contactDetails.put("Problema",problem.name());
        String contactTitle="Ocorrência recebida: "+problem.name();
        var contactMessage=new EmailService.HtmlMessage(problem.email(),contactTitle,plainText(contactTitle,problem.defaultResponse(),contactDetails,images.size()),TEMPLATE,model(contactTitle,problem.defaultResponse(),contactDetails),images);

        AfterCommit.run(()->{
            log.info("afterCommit fired for occurrence {}: dispatching {} recipient e-mail(s) + contact {}",occurrence.getId(),recipients.size(),problem.email());
            recipients.forEach(address->email.sendHtml(new EmailService.HtmlMessage(address,notification.subject(),notification.plainText(),notification.template(),notification.model(),notification.images())));
            email.sendHtml(contactMessage);
        });
        log.info("Occurrence {} create() returning; e-mail dispatch registered for after commit",occurrence.getId());
        return mapper.toResponse(occurrence);
    }
    @Transactional(readOnly=true) public Page<OccurrenceDtos.Response> list(Pageable pageable){return occurrences.findAll(pageable).map(mapper::toResponse);}

    /**
     * Reads the images for the notification e-mail without ever failing the occurrence. A bad image
     * (too large, unsupported type, malformed base64, read error) is logged and the occurrence is kept;
     * the e-mail is simply sent without the images that could not be read.
     */
    private List<EmailService.InlineImage> readImagesBestEffort(List<MultipartFile> files,List<String> encoded,List<OccurrenceDtos.ImageContent> items,UUID occurrenceId){
        try { return imageReader.read(files,encoded,items); }
        catch (RuntimeException ex){
            log.warn("Could not read the image(s) for occurrence {}; the occurrence was saved and the e-mail will be sent without them: {}",occurrenceId,ex.getMessage());
            return List.of();
        }
    }

    private static Map<String,Object> model(String title,String message,Map<String,String> details){
        Map<String,Object> model=new LinkedHashMap<>();
        model.put("title",title);model.put("message",message);model.put("details",details);
        return model;
    }

    private static String plainText(String title,String message,Map<String,String> details,int imageCount){
        StringBuilder text=new StringBuilder(title).append("\n\n").append(message).append("\n\n");
        text.append(details.entrySet().stream().map(e->e.getKey()+": "+e.getValue()).collect(Collectors.joining("\n")));
        if(imageCount>0)text.append("\n\n").append(imageCount).append(imageCount==1?" imagem enviada (veja a versão HTML do e-mail).":" imagens enviadas (veja a versão HTML do e-mail).");
        return text.toString();
    }
}

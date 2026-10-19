package com.fivesense.api.occurrences.app;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.users.app.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OccurrenceServiceTests {
    private OccurrenceRepository occurrences;private OccurrenceMapper mapper;private ProblemService problems;private MaterialService materials;private UserService users;private EmailService email;private OccurrenceImageReader imageReader;private OccurrenceService service;
    private final UUID problemId=UUID.randomUUID(),materialId=UUID.randomUUID(),actor=UUID.randomUUID();

    @BeforeEach void setup(){
        occurrences=mock(OccurrenceRepository.class);mapper=mock(OccurrenceMapper.class);problems=mock(ProblemService.class);materials=mock(MaterialService.class);users=mock(UserService.class);email=mock(EmailService.class);imageReader=mock(OccurrenceImageReader.class);
        service=new OccurrenceService(occurrences,mapper,problems,materials,users,email,imageReader);
        when(problems.requireActive(problemId)).thenReturn(new ProblemService.NotificationDetails(problemId,"Vassoura","quality@example.com","Recebemos o aviso"));
        when(materials.requireActive(materialId)).thenReturn("Vassouras");when(users.requireActive(actor)).thenReturn("Ana");
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv->inv.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),problemId,materialId,actor,3,Instant.now()));
        when(imageReader.read(any(),any(),any())).thenReturn(List.of());
    }

    private OccurrenceDtos.CreateRequest request(){return new OccurrenceDtos.CreateRequest(problemId,materialId,3,actor,null);}
    private List<EmailService.HtmlMessage> sentMessages(){var captor=ArgumentCaptor.forClass(EmailService.HtmlMessage.class);verify(email,atLeastOnce()).sendHtml(captor.capture());return captor.getAllValues();}

    @Test void createPersistsAndQueuesHtmlEmailsForActiveAdministratorsAndProblemContact(){
        when(users.notificationRecipients()).thenReturn(List.of("a@example.com","m@example.com"));
        var result=service.create(request(),null,null);
        assertThat(result).isNotNull();
        var saved=ArgumentCaptor.forClass(Occurrence.class);verify(occurrences).save(saved.capture());
        assertThat(saved.getValue().getReportedByUserId()).isEqualTo(actor);assertThat(saved.getValue().getAffectedQuantity()).isEqualTo(3);
        var messages=sentMessages();
        assertThat(messages).extracting(EmailService.HtmlMessage::recipient).containsExactly("a@example.com","m@example.com","quality@example.com");
        assertThat(messages).extracting(EmailService.HtmlMessage::template).containsOnly("email/occurrence");
        var notification=messages.get(0);
        assertThat(notification.subject()).contains("Nova ocorrência").contains("Vassoura");
        assertThat(notification.model().get("details")).isEqualTo(Map.of("Problema","Vassoura","Material","Vassouras","Quantidade afetada","3","Registrada por","Ana","Ocorrência",String.valueOf(saved.getValue().getId())));
        assertThat(notification.plainText()).contains("Quantidade afetada: 3").contains("Material: Vassouras");
        var contact=messages.get(2);
        assertThat(contact.subject()).isEqualTo("Ocorrência recebida: Vassoura");
        assertThat(contact.model().get("message")).isEqualTo("Recebemos o aviso");
    }

    @Test void createEmbedsTheSameImagesInEveryEmail(){
        var image=new EmailService.InlineImage("image1","foto.png","image/png",new byte[]{1,2});
        var files=List.<org.springframework.web.multipart.MultipartFile>of(new MockMultipartFile("images","foto.png","image/png",new byte[]{1}));
        when(imageReader.read(eq(files),eq(List.of("b64")),any())).thenReturn(List.of(image));
        when(users.notificationRecipients()).thenReturn(List.of("a@example.com"));
        service.create(request(),files,List.of("b64"));
        var messages=sentMessages();
        assertThat(messages).hasSize(2).allSatisfy(m->assertThat(m.images()).containsExactly(image));
        assertThat(messages.get(0).plainText()).contains("1 imagem enviada");
    }

    @Test void createStillNotifiesTheProblemContactWhenThereIsNoActiveAdministrator(){
        when(users.notificationRecipients()).thenReturn(List.of());
        service.create(request(),null,null);
        assertThat(sentMessages()).extracting(EmailService.HtmlMessage::recipient).containsExactly("quality@example.com");
    }

    @Test void createReadsImagesFromTheJsonBodyToo(){
        var items=List.of(new OccurrenceDtos.ImageContent("a.png","AQID"));
        when(users.notificationRecipients()).thenReturn(List.of());
        service.create(new OccurrenceDtos.CreateRequest(problemId,materialId,3,actor,items),null,null);
        verify(imageReader).read(null,null,items);
    }

    @Test void createStopsWhenProblemIsUnavailable(){
        UUID other=UUID.randomUUID();when(problems.requireActive(other)).thenThrow(ApiException.badRequest("Problem is unavailable"));
        assertThatThrownBy(()->service.create(new OccurrenceDtos.CreateRequest(other,materialId,1,actor,null),null,null)).isInstanceOf(ApiException.class);
        verifyNoInteractions(occurrences,email);
    }

    @Test void createStopsWhenTheReportingUserDoesNotExistOrIsInactive(){
        UUID unknown=UUID.randomUUID();when(users.requireActive(unknown)).thenThrow(ApiException.badRequest("Reporting user is unavailable"));
        assertThatThrownBy(()->service.create(new OccurrenceDtos.CreateRequest(problemId,materialId,1,unknown,null),null,null)).hasMessageContaining("Reporting user");
        verifyNoInteractions(occurrences,email);
    }

    @Test void createRejectsInvalidImagesBeforeAnyDatabaseWork(){
        when(imageReader.read(any(),any(),any())).thenThrow(ApiException.badRequest("Unsupported image type"));
        assertThatThrownBy(()->service.create(request(),null,null)).isInstanceOf(ApiException.class);
        verifyNoInteractions(occurrences,email,problems,materials,users);
    }

    @Test void listMapsOccurrencePage(){Occurrence occurrence=mock(Occurrence.class);when(occurrences.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(occurrence)));when(mapper.toResponse(occurrence)).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,Instant.now()));assertThat(service.list(PageRequest.of(0,20))).hasSize(1);}
}

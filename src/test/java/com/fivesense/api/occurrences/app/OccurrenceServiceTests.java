package com.fivesense.api.occurrences.app;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.occurrences.infra.TemporaryImageStore;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.users.app.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OccurrenceServiceTests {
    private OccurrenceRepository occurrences;private OccurrenceMapper mapper;private ProblemService problems;private MaterialService materials;private UserService users;private EmailService email;private TemporaryImageStore images;private OccurrenceService service;
    @BeforeEach void setup(){occurrences=mock(OccurrenceRepository.class);mapper=mock(OccurrenceMapper.class);problems=mock(ProblemService.class);materials=mock(MaterialService.class);users=mock(UserService.class);email=mock(EmailService.class);images=mock(TemporaryImageStore.class);service=new OccurrenceService(occurrences,mapper,problems,materials,users,email,images);}

    @Test void createPersistsAndNotifiesActiveAdministratorsAndProblemContact(){
        UUID problemId=UUID.randomUUID(),materialId=UUID.randomUUID(),actor=UUID.randomUUID();var details=new ProblemService.NotificationDetails(problemId,"P","quality@example.com","Received");
        when(problems.requireActive(problemId)).thenReturn(details);when(users.notificationRecipients()).thenReturn(List.of("a@example.com","m@example.com"));
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),problemId,materialId,actor,3,Instant.now()));
        when(images.locate(any())).thenReturn(Optional.empty());
        var result=service.create(actor,new OccurrenceDtos.CreateRequest(problemId,materialId,3));assertThat(result).isNotNull();verify(materials).requireActive(materialId);
        verify(email).send(eq("a@example.com"),contains("Nova ocorrência"),contains("Quantidade afetada: 3"));verify(email).send(eq("m@example.com"),anyString(),anyString());verify(email).send("quality@example.com","Ocorrência recebida: P","Received");
    }

    @Test void createWithImageSendsAttachmentsAndDeletesTempFile(){
        UUID problemId=UUID.randomUUID(),materialId=UUID.randomUUID(),actor=UUID.randomUUID(),imageId=UUID.randomUUID();
        var details=new ProblemService.NotificationDetails(problemId,"P","quality@example.com","Received");Path temp=Path.of("temp-image");
        when(problems.requireActive(problemId)).thenReturn(details);when(users.notificationRecipients()).thenReturn(List.of("a@example.com"));
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),problemId,materialId,actor,2,Instant.now()));
        when(images.locate(imageId)).thenReturn(Optional.of(new TemporaryImageStore.StoredImage(temp,"foto.png")));
        service.create(actor,new OccurrenceDtos.CreateRequest(problemId,materialId,2,actor,imageId));
        var expected=List.of(new EmailService.Attachment(temp,"foto.png"));
        verify(email).sendWithAttachments(eq("a@example.com"),contains("Nova ocorrência"),anyString(),eq(expected));
        verify(email).sendWithAttachments(eq("quality@example.com"),contains("Ocorrência recebida"),eq("Received"),eq(expected));
        verify(email,never()).send(anyString(),anyString(),anyString());
        verify(images).delete(imageId);
    }

    @Test void createWithSeveralImagesAttachesAllOfThemAndDeletesEveryTempFile(){
        UUID problemId=UUID.randomUUID(),materialId=UUID.randomUUID(),actor=UUID.randomUUID(),first=UUID.randomUUID(),second=UUID.randomUUID(),third=UUID.randomUUID();
        var details=new ProblemService.NotificationDetails(problemId,"P","quality@example.com","Received");
        when(problems.requireActive(problemId)).thenReturn(details);when(users.notificationRecipients()).thenReturn(List.of("a@example.com"));
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),problemId,materialId,actor,2,Instant.now()));
        when(images.locate(first)).thenReturn(Optional.of(new TemporaryImageStore.StoredImage(Path.of("one"),"um.png")));
        when(images.locate(second)).thenReturn(Optional.empty());
        when(images.locate(third)).thenReturn(Optional.of(new TemporaryImageStore.StoredImage(Path.of("three"),"tres.jpg")));
        // the legacy imageId repeated inside imageIds must be attached only once
        service.create(actor,new OccurrenceDtos.CreateRequest(problemId,materialId,2,actor,first,List.of(first,second,third)));
        var expected=List.of(new EmailService.Attachment(Path.of("one"),"um.png"),new EmailService.Attachment(Path.of("three"),"tres.jpg"));
        verify(email).sendWithAttachments(eq("a@example.com"),anyString(),anyString(),eq(expected));
        verify(email).sendWithAttachments(eq("quality@example.com"),anyString(),eq("Received"),eq(expected));
        verify(images).delete(first);verify(images).delete(second);verify(images).delete(third);
    }

    @Test void createFallsBackToPlainEmailWhenEveryTempImageIsGone(){
        UUID problemId=UUID.randomUUID(),materialId=UUID.randomUUID(),actor=UUID.randomUUID(),imageId=UUID.randomUUID();
        var details=new ProblemService.NotificationDetails(problemId,"P","quality@example.com","Received");
        when(problems.requireActive(problemId)).thenReturn(details);when(users.notificationRecipients()).thenReturn(List.of("a@example.com"));
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),problemId,materialId,actor,1,Instant.now()));
        when(images.locate(imageId)).thenReturn(Optional.empty());
        service.create(actor,new OccurrenceDtos.CreateRequest(problemId,materialId,1,actor,null,List.of(imageId)));
        verify(email).send(eq("a@example.com"),anyString(),anyString());verify(email).send("quality@example.com","Ocorrência recebida: P","Received");
        verify(email,never()).sendWithAttachments(anyString(),anyString(),anyString(),anyList());
    }

    @Test void createRejectsMoreThanTheMaximumNumberOfImagesBeforePersisting(){
        UUID problemId=UUID.randomUUID(),actor=UUID.randomUUID();
        when(problems.requireActive(problemId)).thenReturn(new ProblemService.NotificationDetails(problemId,"P","quality@example.com","Received"));
        var tooMany=java.util.stream.Stream.generate(UUID::randomUUID).limit(OccurrenceDtos.MAX_IMAGES).toList();
        assertThatThrownBy(()->service.create(actor,new OccurrenceDtos.CreateRequest(problemId,UUID.randomUUID(),1,actor,UUID.randomUUID(),tooMany))).isInstanceOf(ApiException.class);
        verifyNoInteractions(occurrences,email);
    }

    @Test void storeImagesStoresFilesAndBase64TextInOrder(){
        UUID fileId=UUID.randomUUID(),textId=UUID.randomUUID();byte[] pngBytes={1,2,3};
        when(images.maxBytes()).thenReturn(1024L);
        when(images.store(any(java.io.InputStream.class),eq("foto.png"))).thenReturn(fileId);
        when(images.store(any(java.io.InputStream.class),isNull())).thenReturn(textId);
        var file=new MockMultipartFile("images","foto.png","image/png",new byte[]{9});
        var result=service.storeImages(List.of(file),List.of("data:image/png;base64,"+Base64.getEncoder().encodeToString(pngBytes)));
        assertThat(result.imageIds()).containsExactly(fileId,textId);assertThat(result.imageId()).isEqualTo(fileId);
        var stored=ArgumentCaptor.forClass(java.io.InputStream.class);verify(images,times(2)).store(stored.capture(),any());
        assertThat(readAll(stored.getAllValues().get(1))).isEqualTo(pngBytes);
    }

    @Test void storeImagesAcceptsBase64WithLineBreaksAndUrlSafeAlphabet(){
        UUID id=UUID.randomUUID();byte[] bytes={(byte)0xFB,(byte)0xFF,(byte)0xFE,1};
        when(images.maxBytes()).thenReturn(1024L);when(images.store(any(java.io.InputStream.class),any())).thenReturn(id);
        String urlSafe=Base64.getUrlEncoder().encodeToString(bytes);
        service.storeImages(new OccurrenceDtos.ImageBatchRequest(List.of(new OccurrenceDtos.ImageContent("a.png",urlSafe.substring(0,3)+"\r\n"+urlSafe.substring(3)))));
        var stored=ArgumentCaptor.forClass(java.io.InputStream.class);verify(images).store(stored.capture(),eq("a.png"));
        assertThat(readAll(stored.getValue())).isEqualTo(bytes);
    }

    @Test void storeImagesDiscardsAlreadyStoredImagesWhenOneIsRejected(){
        UUID first=UUID.randomUUID();
        when(images.maxBytes()).thenReturn(1024L);
        when(images.store(any(java.io.InputStream.class),eq("ok.png"))).thenReturn(first);
        var request=new OccurrenceDtos.ImageBatchRequest(List.of(new OccurrenceDtos.ImageContent("ok.png","AQID"),new OccurrenceDtos.ImageContent("bad.png","***not base64***")));
        assertThatThrownBy(()->service.storeImages(request)).isInstanceOfSatisfying(ApiException.class,ex->assertThat(ex.getStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST));
        verify(images).delete(first);
    }

    @Test void storeImagesRejectsEmptyAndOversizedBatches(){
        assertThatThrownBy(()->service.storeImages(null,null)).isInstanceOf(ApiException.class);
        assertThatThrownBy(()->service.storeImages(List.of(),List.of())).isInstanceOf(ApiException.class);
        var files=java.util.stream.Stream.generate(()->(org.springframework.web.multipart.MultipartFile)new MockMultipartFile("images","f.png","image/png",new byte[]{1})).limit(OccurrenceDtos.MAX_IMAGES+1).toList();
        assertThatThrownBy(()->service.storeImages(files,null)).isInstanceOf(ApiException.class);
        verify(images,never()).store(any(),any());
    }

    @Test void storeImagesRejectsEncodedImageLargerThanTheLimitWithoutDecoding(){
        when(images.maxBytes()).thenReturn(3L);
        var request=new OccurrenceDtos.ImageBatchRequest(List.of(new OccurrenceDtos.ImageContent("a.png","A".repeat(100))));
        assertThatThrownBy(()->service.storeImages(request)).isInstanceOfSatisfying(ApiException.class,ex->assertThat(ex.getStatus()).isEqualTo(org.springframework.http.HttpStatus.CONTENT_TOO_LARGE));
        verify(images,never()).store(any(),any());
    }

    private static byte[] readAll(java.io.InputStream in){try{return in.readAllBytes();}catch(java.io.IOException ex){throw new IllegalStateException(ex);}}

    @Test void createStopsWhenProblemIsUnavailable(){UUID problemId=UUID.randomUUID();when(problems.requireActive(problemId)).thenThrow(ApiException.badRequest("Problem is unavailable"));assertThatThrownBy(()->service.create(UUID.randomUUID(),new OccurrenceDtos.CreateRequest(problemId,UUID.randomUUID(),1))).isInstanceOf(ApiException.class);verifyNoInteractions(occurrences,email);}
    @Test void listMapsOccurrencePage(){Occurrence occurrence=mock(Occurrence.class);when(occurrences.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(occurrence)));when(mapper.toResponse(occurrence)).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,Instant.now()));assertThat(service.list(PageRequest.of(0,20))).hasSize(1);}
}

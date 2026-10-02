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
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OccurrenceServiceTests {
    private OccurrenceRepository occurrences;private OccurrenceMapper mapper;private ProblemService problems;private MaterialService materials;private UserService users;private EmailService email;private OccurrenceService service;
    @BeforeEach void setup(){occurrences=mock(OccurrenceRepository.class);mapper=mock(OccurrenceMapper.class);problems=mock(ProblemService.class);materials=mock(MaterialService.class);users=mock(UserService.class);email=mock(EmailService.class);service=new OccurrenceService(occurrences,mapper,problems,materials,users,email);}

    @Test void createPersistsAndNotifiesActiveAdministratorsAndProblemContact(){
        UUID problemId=UUID.randomUUID(),materialId=UUID.randomUUID(),actor=UUID.randomUUID();var details=new ProblemService.NotificationDetails(problemId,"P","quality@example.com","Received");
        when(problems.requireActive(problemId)).thenReturn(details);when(users.notificationRecipients()).thenReturn(List.of("a@example.com","m@example.com"));
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),problemId,materialId,actor,3,Instant.now()));
        var result=service.create(actor,new OccurrenceDtos.CreateRequest(problemId,materialId,3));assertThat(result).isNotNull();verify(materials).requireActive(materialId);
        verify(email).send(eq("a@example.com"),contains("Nova ocorrência"),contains("Quantidade afetada: 3"));verify(email).send(eq("m@example.com"),anyString(),anyString());verify(email).send("quality@example.com","Ocorrência recebida: P","Received");
    }
    @Test void createStopsWhenProblemIsUnavailable(){UUID problemId=UUID.randomUUID();when(problems.requireActive(problemId)).thenThrow(ApiException.badRequest("Problem is unavailable"));assertThatThrownBy(()->service.create(UUID.randomUUID(),new OccurrenceDtos.CreateRequest(problemId,UUID.randomUUID(),1))).isInstanceOf(ApiException.class);verifyNoInteractions(occurrences,email);}
    @Test void listMapsOccurrencePage(){Occurrence occurrence=mock(Occurrence.class);when(occurrences.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(occurrence)));when(mapper.toResponse(occurrence)).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,Instant.now()));assertThat(service.list(PageRequest.of(0,20))).hasSize(1);}
}

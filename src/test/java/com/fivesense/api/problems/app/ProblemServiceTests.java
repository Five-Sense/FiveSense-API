package com.fivesense.api.problems.app;

import com.fivesense.api.problems.domain.Problem;
import com.fivesense.api.problems.dto.ProblemDtos;
import com.fivesense.api.problems.infra.ProblemRepository;
import com.fivesense.api.shared.error.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProblemServiceTests {
    private ProblemRepository problems;private ProblemMapper mapper;private ProblemService service;
    @BeforeEach void setup(){problems=mock(ProblemRepository.class);mapper=mock(ProblemMapper.class);service=new ProblemService(problems,mapper);}

    @Test void listMapsResponses(){Problem p=problem();when(problems.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));when(mapper.toResponse(p)).thenReturn(response());assertThat(service.list(PageRequest.of(0,20))).hasSize(1);}
    @Test void getAndOccurrenceOptionsReturnAvailableRecords(){UUID id=UUID.randomUUID();Problem p=problem();when(problems.findById(id)).thenReturn(Optional.of(p));when(mapper.toResponse(p)).thenReturn(response());assertThat(service.get(id)).isNotNull();when(problems.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(p));when(mapper.toOption(p)).thenReturn(new ProblemDtos.Option(id,"P"));assertThat(service.optionsForOccurrence()).hasSize(1);}
    @Test void requireActiveReturnsNotificationFieldsOrRejectsInactive(){UUID id=UUID.randomUUID();Problem p=problem();when(problems.findById(id)).thenReturn(Optional.of(p));assertThat(service.requireActive(id).email()).isEqualTo("quality@example.com");p.update("P","quality@example.com","ok",false,Instant.now());assertThatThrownBy(()->service.requireActive(id)).isInstanceOf(ApiException.class);}
    @Test void createNormalizesEmailAndMapsSavedProblem(){when(problems.save(any(Problem.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(response());service.create(new ProblemDtos.UpsertRequest("P"," QUALITY@EXAMPLE.COM ","ok",true));verify(problems).save(argThat(p->p.getRelatedEmail().equals("quality@example.com")));}
    @Test void updateAppliesActiveDefaultAndRejectsMissing(){UUID id=UUID.randomUUID();Problem p=problem();when(problems.findById(id)).thenReturn(Optional.of(p));when(mapper.toResponse(p)).thenReturn(response());service.update(id,new ProblemDtos.UpsertRequest("Q","q@example.com","new",null));assertThat(p.getName()).isEqualTo("Q");assertThat(p.isActive()).isTrue();when(problems.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.update(id,new ProblemDtos.UpsertRequest("Q","q@example.com","new",true))).isInstanceOf(ApiException.class);}
    @Test void deleteRejectsReferencedProblem(){UUID id=UUID.randomUUID();Problem p=problem();when(problems.findById(id)).thenReturn(Optional.of(p));doThrow(new DataIntegrityViolationException("fk")).when(problems).flush();assertThatThrownBy(()->service.delete(id)).isInstanceOf(ApiException.class);}
    @Test void deleteRejectsMissingProblem(){UUID id=UUID.randomUUID();when(problems.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.delete(id)).isInstanceOf(ApiException.class);}
    private static Problem problem(){return new Problem("P","quality@example.com","ok",Instant.now());}
    private static ProblemDtos.Response response(){return new ProblemDtos.Response(UUID.randomUUID(),"P","quality@example.com","ok",true);}
}

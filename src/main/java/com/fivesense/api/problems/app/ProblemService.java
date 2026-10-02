package com.fivesense.api.problems.app;

import com.fivesense.api.problems.domain.Problem;
import com.fivesense.api.problems.dto.ProblemDtos;
import com.fivesense.api.problems.infra.ProblemRepository;
import com.fivesense.api.shared.error.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class ProblemService {
    private final ProblemRepository problems;private final ProblemMapper mapper;
    public ProblemService(ProblemRepository problems,ProblemMapper mapper){this.problems=problems;this.mapper=mapper;}
    @Transactional(readOnly=true) public Page<ProblemDtos.Response> list(Pageable pageable){return problems.findAll(pageable).map(mapper::toResponse);}
    @Transactional(readOnly=true) public ProblemDtos.Response get(UUID id){return mapper.toResponse(problems.findById(id).orElseThrow(()->ApiException.notFound("Problem")));}
    @Transactional(readOnly=true) public List<ProblemDtos.Option> optionsForOccurrence(){return problems.findByActiveTrueOrderByNameAsc().stream().map(mapper::toOption).toList();}
    @Transactional(readOnly=true) public NotificationDetails requireActive(UUID id){Problem p=problems.findById(id).filter(Problem::isActive).orElseThrow(()->ApiException.badRequest("Problem is unavailable"));return new NotificationDetails(p.getId(),p.getName(),p.getRelatedEmail(),p.getDefaultResponse());}
    @Transactional public ProblemDtos.Response create(ProblemDtos.UpsertRequest request){var now=Instant.now();return mapper.toResponse(problems.save(new Problem(request.name().trim(),request.relatedEmail().trim().toLowerCase(Locale.ROOT),request.defaultResponse().trim(),now)));}
    @Transactional public ProblemDtos.Response update(UUID id,ProblemDtos.UpsertRequest request){Problem p=problems.findById(id).orElseThrow(()->ApiException.notFound("Problem"));boolean active=request.active()==null?p.isActive():request.active();p.update(request.name().trim(),request.relatedEmail().trim().toLowerCase(Locale.ROOT),request.defaultResponse().trim(),active,Instant.now());return mapper.toResponse(p);}
    @Transactional public void delete(UUID id){Problem p=problems.findById(id).orElseThrow(()->ApiException.notFound("Problem"));try{problems.delete(p);problems.flush();}catch(DataIntegrityViolationException ex){throw ApiException.conflict("Problem is referenced by an occurrence");}}
    public record NotificationDetails(UUID id,String name,String email,String defaultResponse){}
}

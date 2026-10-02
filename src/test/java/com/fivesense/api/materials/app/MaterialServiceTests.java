package com.fivesense.api.materials.app;

import com.fivesense.api.materials.domain.Material;
import com.fivesense.api.materials.dto.MaterialDtos;
import com.fivesense.api.materials.infra.MaterialRepository;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.users.app.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MaterialServiceTests {
    private MaterialRepository materials;private MaterialMapper mapper;private UserService users;private EmailService email;private MaterialService service;
    @BeforeEach void setup(){materials=mock(MaterialRepository.class);mapper=mock(MaterialMapper.class);users=mock(UserService.class);email=mock(EmailService.class);service=new MaterialService(materials,mapper,users,email);}

    @Test void listAndGetMapMaterialResponses(){UUID id=UUID.randomUUID();Material m=material(8,2);when(materials.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(m)));when(mapper.toResponse(m)).thenReturn(response());assertThat(service.list(PageRequest.of(0,20))).hasSize(1);when(materials.findById(id)).thenReturn(Optional.of(m));when(mapper.toResponse(m)).thenReturn(response());assertThat(service.get(id)).isNotNull();}
    @Test void stockAndOccurrenceOptionsMapOnlyActiveMaterials(){Material m=material(8,2);when(materials.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(m));when(mapper.toStockView(m)).thenReturn(new MaterialDtos.StockView(UUID.randomUUID(),"M",8,2,false));when(mapper.toOption(m)).thenReturn(new MaterialDtos.Option(UUID.randomUUID(),"M"));assertThat(service.stock()).hasSize(1);assertThat(service.optionsForOccurrence()).hasSize(1);}
    @Test void requireActiveRejectsMissingAndInactive(){UUID id=UUID.randomUUID();Material m=material(8,2);when(materials.findById(id)).thenReturn(Optional.of(m));service.requireActive(id);m.update("M",8,2,false,Instant.now());assertThatThrownBy(()->service.requireActive(id)).isInstanceOf(ApiException.class);}
    @Test void createSendsAlertOnlyWhenInitiallyAtOrBelowMinimum(){when(materials.save(any(Material.class))).thenAnswer(inv->inv.getArgument(0));when(mapper.toResponse(any())).thenReturn(response());when(users.notificationRecipients()).thenReturn(List.of("manager@example.com"));service.create(new MaterialDtos.UpsertRequest("M",8,2,true));verifyNoInteractions(email);service.create(new MaterialDtos.UpsertRequest("Low",1,2,true));verify(email).send(eq("manager@example.com"),contains("estoque baixo"),contains("limite mínimo"));}
    @Test void updateKeepsActiveWhenOmittedAndAlertsOnTransitionBelowMinimum(){UUID id=UUID.randomUUID();Material m=material(8,2);when(materials.findById(id)).thenReturn(Optional.of(m));when(mapper.toResponse(m)).thenReturn(response());when(users.notificationRecipients()).thenReturn(List.of("admin@example.com"));service.update(id,new MaterialDtos.UpsertRequest("M",1,2,null));assertThat(m.isActive()).isTrue();verify(email).send(eq("admin@example.com"),anyString(),anyString());}
    @Test void updateDoesNotRepeatLowStockAlertWhileStillBelow(){UUID id=UUID.randomUUID();Material m=material(1,2);when(materials.findById(id)).thenReturn(Optional.of(m));when(mapper.toResponse(m)).thenReturn(response());service.update(id,new MaterialDtos.UpsertRequest("M",0,2,true));verifyNoInteractions(email);}
    @Test void deleteRejectsMissingOrReferencedMaterials(){UUID id=UUID.randomUUID();when(materials.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.delete(id)).isInstanceOf(ApiException.class);when(materials.findById(id)).thenReturn(Optional.of(material(1,2)));doThrow(new DataIntegrityViolationException("fk")).when(materials).flush();assertThatThrownBy(()->service.delete(id)).isInstanceOf(ApiException.class);}
    private static Material material(int stock,int minimum){return new Material("M",stock,minimum,Instant.now());}
    private static MaterialDtos.Response response(){return new MaterialDtos.Response(UUID.randomUUID(),"M",8,2,true,false);}
}

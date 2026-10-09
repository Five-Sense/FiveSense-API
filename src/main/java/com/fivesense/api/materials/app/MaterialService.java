package com.fivesense.api.materials.app;

import com.fivesense.api.materials.domain.Material;
import com.fivesense.api.materials.dto.MaterialDtos;
import com.fivesense.api.materials.infra.MaterialRepository;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.shared.app.AfterCommit;
import com.fivesense.api.users.app.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class MaterialService {
    private final MaterialRepository materials;private final MaterialMapper mapper;private final UserService users;private final EmailService email;
    public MaterialService(MaterialRepository materials,MaterialMapper mapper,UserService users,EmailService email){this.materials=materials;this.mapper=mapper;this.users=users;this.email=email;}
    @Transactional(readOnly=true) public Page<MaterialDtos.Response> list(Pageable pageable){return materials.findAll(pageable).map(mapper::toResponse);}
    @Transactional(readOnly=true) public MaterialDtos.Response get(UUID id){return mapper.toResponse(materials.findById(id).orElseThrow(()->ApiException.notFound("Material")));}
    @Transactional(readOnly=true) public List<MaterialDtos.StockView> stock(){return materials.findByActiveTrueOrderByNameAsc().stream().map(mapper::toStockView).toList();}
    @Transactional(readOnly=true) public List<MaterialDtos.Option> optionsForOccurrence(){return materials.findByActiveTrueOrderByNameAsc().stream().map(mapper::toOption).toList();}
    @Transactional(readOnly=true) public void requireActive(UUID id){materials.findById(id).filter(Material::isActive).orElseThrow(()->ApiException.badRequest("Material is unavailable"));}
    @Transactional public MaterialDtos.Response create(MaterialDtos.UpsertRequest request){Instant now=Instant.now();Material material=materials.save(new Material(request.name().trim(),request.stockQuantity(),request.minimumStock(),now));notifyLowStockIfNeeded(material,true,false);return mapper.toResponse(material);}
    @Transactional public MaterialDtos.Response update(UUID id,MaterialDtos.UpsertRequest request){Material material=materials.findById(id).orElseThrow(()->ApiException.notFound("Material"));boolean wasLow=low(material);material.update(request.name().trim(),request.stockQuantity(),request.minimumStock(),request.active()==null?material.isActive():request.active(),Instant.now());notifyLowStockIfNeeded(material,!wasLow,wasLow);return mapper.toResponse(material);}
    @Transactional public void delete(UUID id){Material material=materials.findById(id).orElseThrow(()->ApiException.notFound("Material"));try{materials.delete(material);materials.flush();}catch(DataIntegrityViolationException ex){throw ApiException.conflict("Material is referenced by an occurrence");}}
    @Transactional public MaterialDtos.Response adjustStock(UUID id,int delta){Material material=materials.findById(id).orElseThrow(()->ApiException.notFound("Material"));if(delta==0)throw ApiException.badRequest("Delta must not be zero");int result=material.getStockQuantity()+delta;if(result<0||result>999)throw ApiException.badRequest("Stock must stay between 0 and 999");boolean wasLow=low(material);material.adjustStock(delta,Instant.now());notifyLowStockIfNeeded(material,!wasLow,wasLow);return mapper.toResponse(material);}
    private static boolean low(Material m){return m.getStockQuantity()<=m.getMinimumStock();}
    private void notifyLowStockIfNeeded(Material material,boolean crossedDown,boolean wasLow){if(low(material)&&crossedDown&&!wasLow){String subject="Alerta de estoque baixo: "+material.getName();String body="O material "+material.getName()+" está com "+material.getStockQuantity()+" unidade(s), no limite mínimo de "+material.getMinimumStock()+".";var recipients=users.notificationRecipients();AfterCommit.run(()->recipients.forEach(address->email.send(address,subject,body)));}}
}

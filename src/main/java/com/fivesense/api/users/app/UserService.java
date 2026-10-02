package com.fivesense.api.users.app;

import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.dto.UserDtos;
import com.fivesense.api.users.infra.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class UserService {
    private final UserRepository users;
    private final UserMapper mapper;
    public UserService(UserRepository users,UserMapper mapper){this.users=users;this.mapper=mapper;}

    @Transactional
    public UserDtos.Response create(UserDtos.CreateRequest request){
        String address=normalize(request.email());
        if(users.existsByEmailIgnoreCase(address))throw ApiException.conflict("Email already exists");
        AppUser created=users.save(new AppUser(request.name().trim(),address,request.password(),request.role(),UserStatus.ACTIVE,Instant.now()));
        return mapper.toResponse(created);
    }

    @Transactional(readOnly=true)
    public Page<UserDtos.Response> list(Pageable pageable){return users.findAll(pageable).map(mapper::toResponse);}

    @Transactional(readOnly=true)
    public UserDtos.Response get(UUID id){return users.findById(id).map(mapper::toResponse).orElseThrow(()->ApiException.notFound("User"));}

    @Transactional
    public UserDtos.Response update(UUID id,UserDtos.UpdateRequest request){
        AppUser target=users.findById(id).orElseThrow(()->ApiException.notFound("User"));
        String address=normalize(request.email());
        users.findByEmailIgnoreCase(address).filter(u->!u.getId().equals(id)).ifPresent(u->{throw ApiException.conflict("Email already exists");});
        target.update(request.name().trim(),address,target.getRole(),request.status(),Instant.now());
        return mapper.toResponse(target);
    }

    @Transactional(readOnly=true)
    public List<String> notificationRecipients(){return users.findByRoleInAndStatus(List.of(UserRole.ADMIN,UserRole.MANAGER),UserStatus.ACTIVE).stream().map(AppUser::getEmail).toList();}

    private static String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
}

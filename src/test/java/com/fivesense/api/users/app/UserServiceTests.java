package com.fivesense.api.users.app;

import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.dto.UserDtos;
import com.fivesense.api.users.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTests {
    private UserRepository users;private UserMapper mapper;private PasswordEncoder passwords;private EmailService email;private UserService service;
    @BeforeEach void setup(){users=mock(UserRepository.class);mapper=mock(UserMapper.class);passwords=mock(PasswordEncoder.class);email=mock(EmailService.class);service=new UserService(users,mapper,passwords,email);when(passwords.encode(anyString())).thenReturn("hash");when(mapper.toResponse(any())).thenReturn(response());}

    @Test void createAllowsAdminManagerAndManagerViewerButNotAdminForManager(){UUID adminId=UUID.randomUUID();AppUser admin=user(adminId,UserRole.ADMIN,UserStatus.ACTIVE);when(users.findById(adminId)).thenReturn(Optional.of(admin));when(users.existsByEmailIgnoreCase(anyString())).thenReturn(false);when(users.save(any())).thenAnswer(inv->inv.getArgument(0));service.create(adminId,new UserDtos.CreateRequest("M","M@EXAMPLE.COM",UserRole.MANAGER));verify(users).save(argThat(u->u.getEmail().equals("m@example.com")&&u.getStatus()==UserStatus.FIRST_ACCESS));verify(email).send(eq("m@example.com"),anyString(),contains("primeiro acesso"));}
    @Test void createRejectsDuplicateAndForbiddenRole(){UUID id=UUID.randomUUID();when(users.findById(id)).thenReturn(Optional.of(user(id,UserRole.ADMIN,UserStatus.ACTIVE)));when(users.existsByEmailIgnoreCase(anyString())).thenReturn(true);assertThatThrownBy(()->service.create(id,new UserDtos.CreateRequest("M","m@example.com",UserRole.VIEWER))).isInstanceOf(ApiException.class);when(users.existsByEmailIgnoreCase(anyString())).thenReturn(false);assertThatThrownBy(()->service.create(id,new UserDtos.CreateRequest("A","a@example.com",UserRole.ADMIN))).isInstanceOf(ApiException.class);}
    @Test void listScopesManagerToViewerAndAdminToAll(){UUID adminId=UUID.randomUUID(),managerId=UUID.randomUUID();when(users.findById(adminId)).thenReturn(Optional.of(user(adminId,UserRole.ADMIN,UserStatus.ACTIVE)));when(users.findById(managerId)).thenReturn(Optional.of(user(managerId,UserRole.MANAGER,UserStatus.ACTIVE)));when(users.findAll(any(Pageable.class))).thenReturn(Page.empty());when(users.findByRoleIn(eq(List.of(UserRole.VIEWER)),any(Pageable.class))).thenReturn(Page.empty());service.list(adminId,PageRequest.of(0,20));service.list(managerId,PageRequest.of(0,20));verify(users).findAll(any(Pageable.class));verify(users).findByRoleIn(eq(List.of(UserRole.VIEWER)),any(Pageable.class));}
    @Test void getRestrictsManagerToViewer(){UUID managerId=UUID.randomUUID(),targetId=UUID.randomUUID();when(users.findById(managerId)).thenReturn(Optional.of(user(managerId,UserRole.MANAGER,UserStatus.ACTIVE)));when(users.findById(targetId)).thenReturn(Optional.of(user(targetId,UserRole.VIEWER,UserStatus.ACTIVE)));assertThat(service.get(managerId,targetId)).isNotNull();when(users.findById(targetId)).thenReturn(Optional.empty());assertThatThrownBy(()->service.get(managerId,targetId)).isInstanceOf(ApiException.class);}
    @Test void updateRejectsFirstAccessAndUpdatesActiveViewer(){UUID actor=UUID.randomUUID(),target=UUID.randomUUID();AppUser manager=user(actor,UserRole.MANAGER,UserStatus.ACTIVE),viewer=user(target,UserRole.VIEWER,UserStatus.FIRST_ACCESS);when(users.findById(actor)).thenReturn(Optional.of(manager));when(users.findById(target)).thenReturn(Optional.of(viewer));assertThatThrownBy(()->service.update(actor,target,new UserDtos.UpdateRequest("V","v@example.com",UserStatus.INACTIVE))).isInstanceOf(ApiException.class);viewer.changePassword("hash",UserStatus.ACTIVE,Instant.now());when(users.findByEmailIgnoreCase("v@example.com")).thenReturn(Optional.of(viewer));service.update(actor,target,new UserDtos.UpdateRequest("New","v@example.com",UserStatus.INACTIVE));assertThat(viewer.getStatus()).isEqualTo(UserStatus.INACTIVE);}
    @Test void resendSendsNewPasswordAndEnforcesRateLimit(){UUID actor=UUID.randomUUID(),target=UUID.randomUUID();when(users.findById(actor)).thenReturn(Optional.of(user(actor,UserRole.ADMIN,UserStatus.ACTIVE)));AppUser viewer=user(target,UserRole.VIEWER,UserStatus.FIRST_ACCESS);when(users.findById(target)).thenReturn(Optional.of(viewer));service.resendInitialPassword(actor,target);verify(email).send(eq(viewer.getEmail()),anyString(),contains("anterior foi invalidada"));assertThatThrownBy(()->service.resendInitialPassword(actor,target)).isInstanceOf(ApiException.class);}
    @Test void notificationRecipientsReturnsAdminAndManagerAddresses(){when(users.findByRoleInAndStatus(List.of(UserRole.ADMIN,UserRole.MANAGER),UserStatus.ACTIVE)).thenReturn(List.of(user(UUID.randomUUID(),UserRole.ADMIN,UserStatus.ACTIVE)));assertThat(service.notificationRecipients()).hasSize(1);}

    private static AppUser user(UUID id,UserRole role,UserStatus status){AppUser u=new AppUser("Test",id+"@example.com","hash",role,status,Instant.now());try{Field field=AppUser.class.getDeclaredField("id");field.setAccessible(true);field.set(u,id);}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}return u;}
    private static UserDtos.Response response(){return new UserDtos.Response(UUID.randomUUID(),"Test","test@example.com",UserRole.VIEWER,UserStatus.ACTIVE,Instant.now(),Instant.now());}
}

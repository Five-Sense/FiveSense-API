package com.fivesense.api.users.app;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.dto.UserDtos;
import com.fivesense.api.users.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
class UserServiceTests {
    private UserRepository users;private UserMapper mapper;private UserService service;
    @BeforeEach void setup(){users=mock(UserRepository.class);mapper=mock(UserMapper.class);service=new UserService(users,mapper);}
    private static AppUser user(String username,String email){AppUser u=new AppUser("N",username,email,"p",UserRole.VIEWER,UserStatus.ACTIVE,Instant.now());org.springframework.test.util.ReflectionTestUtils.setField(u,"id",UUID.randomUUID());return u;}
    private static UserDtos.Response response(){return new UserDtos.Response(UUID.randomUUID(),"N","u","u@x.com",UserRole.VIEWER,UserStatus.ACTIVE,Instant.now(),Instant.now());}
    @Test void createNormalizesUsernameAndEmailAndStoresRole(){when(users.save(any(AppUser.class))).thenAnswer(i->i.getArgument(0));when(mapper.toResponse(any())).thenReturn(response());service.create(new UserDtos.CreateRequest(" Ana ","  Ana.S ","ANA@X.com","pw",UserRole.MANAGER));var captor=org.mockito.ArgumentCaptor.forClass(AppUser.class);verify(users).save(captor.capture());assertThat(captor.getValue().getUsername()).isEqualTo("ana.s");assertThat(captor.getValue().getEmail()).isEqualTo("ana@x.com");assertThat(captor.getValue().getRole()).isEqualTo(UserRole.MANAGER);assertThat(captor.getValue().getPassword()).isEqualTo("pw");}
    @Test void createRejectsDuplicateEmailAndUsername(){when(users.existsByEmailIgnoreCase("a@x.com")).thenReturn(true);assertThatThrownBy(()->service.create(new UserDtos.CreateRequest("A","a","a@x.com","p",UserRole.VIEWER))).isInstanceOf(ApiException.class).hasMessageContaining("Email");when(users.existsByUsernameIgnoreCase("b")).thenReturn(true);assertThatThrownBy(()->service.create(new UserDtos.CreateRequest("B","b","b@x.com","p",UserRole.VIEWER))).isInstanceOf(ApiException.class).hasMessageContaining("Username");}
    @Test void listAndGetMapUsers(){AppUser u=user("u","u@x.com");UUID id=UUID.randomUUID();when(users.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u)));when(mapper.toResponse(u)).thenReturn(response());assertThat(service.list(PageRequest.of(0,20))).hasSize(1);when(users.findById(id)).thenReturn(Optional.of(u));assertThat(service.get(id)).isNotNull();when(users.findById(UUID.randomUUID())).thenReturn(Optional.empty());assertThatThrownBy(()->service.get(UUID.randomUUID())).isInstanceOf(ApiException.class);}
    @Test void updateChangesUsernameKeepsRoleAndRejectsConflicts(){AppUser u=user("old","old@x.com");UUID id=UUID.randomUUID();when(users.findById(id)).thenReturn(Optional.of(u));when(mapper.toResponse(u)).thenReturn(response());service.update(id,new UserDtos.UpdateRequest("N2","New","new@x.com",UserStatus.INACTIVE));assertThat(u.getUsername()).isEqualTo("new");assertThat(u.getRole()).isEqualTo(UserRole.VIEWER);assertThat(u.getStatus()).isEqualTo(UserStatus.INACTIVE);when(users.findByUsernameIgnoreCase("taken")).thenReturn(Optional.of(user("taken","t@x.com")));assertThatThrownBy(()->service.update(id,new UserDtos.UpdateRequest("N","taken","ok@x.com",UserStatus.ACTIVE))).isInstanceOf(ApiException.class).hasMessageContaining("Username");when(users.findByEmailIgnoreCase("dup@x.com")).thenReturn(Optional.of(user("d","dup@x.com")));assertThatThrownBy(()->service.update(id,new UserDtos.UpdateRequest("N","free","dup@x.com",UserStatus.ACTIVE))).isInstanceOf(ApiException.class).hasMessageContaining("Email");}
    @Test void updateFailsForMissingUser(){UUID id=UUID.randomUUID();when(users.findById(id)).thenReturn(Optional.empty());assertThatThrownBy(()->service.update(id,new UserDtos.UpdateRequest("N","u","u@x.com",UserStatus.ACTIVE))).isInstanceOf(ApiException.class);}
    @Test void notificationRecipientsReturnsActiveAdminAndManagerEmails(){when(users.findByRoleInAndStatus(List.of(UserRole.ADMIN,UserRole.MANAGER),UserStatus.ACTIVE)).thenReturn(List.of(user("a","a@x.com")));assertThat(service.notificationRecipients()).containsExactly("a@x.com");}
}

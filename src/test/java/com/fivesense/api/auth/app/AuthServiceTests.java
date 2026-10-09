package com.fivesense.api.auth.app;
import com.fivesense.api.auth.dto.AuthDtos;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.*;
import com.fivesense.api.users.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthServiceTests {
    private UserRepository users;private AuthService service;
    @BeforeEach void setup(){users=mock(UserRepository.class);service=new AuthService(users);}
    private static AppUser admin(UserStatus status){return new AppUser("admin","admin","admin@gmail.com","admin123",UserRole.ADMIN,status,Instant.now());}
    @Test void loginByUsernameReturnsRole(){when(users.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin(UserStatus.ACTIVE)));var r=service.login(new AuthDtos.LoginRequest(" Admin ","admin123"));assertThat(r.authenticated()).isTrue();assertThat(r.role()).isEqualTo(UserRole.ADMIN);assertThat(r.username()).isEqualTo("admin");}
    @Test void loginRejectsUnknownUserWrongPasswordAndInactive(){when(users.findByUsernameIgnoreCase("x")).thenReturn(Optional.empty());assertThatThrownBy(()->service.login(new AuthDtos.LoginRequest("x","p"))).isInstanceOf(ApiException.class);when(users.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin(UserStatus.ACTIVE)));assertThatThrownBy(()->service.login(new AuthDtos.LoginRequest("admin","wrong"))).isInstanceOf(ApiException.class);when(users.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin(UserStatus.INACTIVE)));assertThatThrownBy(()->service.login(new AuthDtos.LoginRequest("admin","admin123"))).isInstanceOf(ApiException.class);}
    @Test void forgotPasswordChangesPasswordOrFailsWhenMissing(){AppUser u=admin(UserStatus.ACTIVE);when(users.findByEmailIgnoreCase("admin@gmail.com")).thenReturn(Optional.of(u));assertThat(service.forgotPassword(new AuthDtos.ForgotPasswordRequest("ADMIN@gmail.com","novo")).updated()).isTrue();assertThat(u.getPassword()).isEqualTo("novo");when(users.findByEmailIgnoreCase("no@x.com")).thenReturn(Optional.empty());assertThatThrownBy(()->service.forgotPassword(new AuthDtos.ForgotPasswordRequest("no@x.com","p"))).isInstanceOf(ApiException.class);}
}

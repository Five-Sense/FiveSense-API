package com.fivesense.api.auth.app;

import com.fivesense.api.auth.domain.BootstrapState;
import com.fivesense.api.auth.dto.AuthDtos;
import com.fivesense.api.auth.infra.BootstrapStateRepository;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.AppUser;
import com.fivesense.api.users.domain.UserRole;
import com.fivesense.api.users.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.lang.reflect.Field;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BootstrapServiceTests {
    private static final String SECRET="test-only-bootstrap-secret-with-32-bytes";
    private BootstrapStateRepository stateRepo;private UserRepository users;private PasswordEncoder passwords;private BootstrapState state;private BootstrapService service;
    @BeforeEach void setup() throws Exception {stateRepo=mock(BootstrapStateRepository.class);users=mock(UserRepository.class);passwords=mock(PasswordEncoder.class);var constructor=BootstrapState.class.getDeclaredConstructor();constructor.setAccessible(true);state=constructor.newInstance();Field id=BootstrapState.class.getDeclaredField("id");id.setAccessible(true);id.set(state,1);when(stateRepo.findById(1)).thenReturn(Optional.of(state));when(passwords.encode(anyString())).thenReturn("bcrypt-hash");when(users.existsByRole(UserRole.ADMIN)).thenReturn(false);when(users.existsByEmailIgnoreCase(anyString())).thenReturn(false);service=new BootstrapService(stateRepo,users,passwords,SECRET,"admin@example.com","Initial Admin");}

    @Test void prepareCreatesAdminAndEncryptsPasswordUntilSingleReveal(){service.prepareInitialAdmin();assertThat(state.getInitialPasswordCiphertext()).isNotBlank();
        var captor=org.mockito.ArgumentCaptor.forClass(AppUser.class);verify(users).save(captor.capture());AppUser saved=captor.getValue();assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);assertThat(saved.getPasswordHash()).isEqualTo("bcrypt-hash");
        when(users.findByRoleIn(List.of(UserRole.ADMIN))).thenReturn(List.of(saved));AuthDtos.BootstrapResponse revealed=service.revealInitialAdminPassword(SECRET);assertThat(revealed.email()).isEqualTo("admin@example.com");assertThat(revealed.initialPassword()).isNotBlank();assertThat(state.getInitialPasswordCiphertext()).isNull();assertThat(state.isConsumed()).isTrue();assertThatThrownBy(()->service.revealInitialAdminPassword(SECRET)).isInstanceOf(ApiException.class);
    }
    @Test void prepareIsIdempotentAndRevealRejectsWrongSecret(){service.prepareInitialAdmin();String ciphertext=state.getInitialPasswordCiphertext();service.prepareInitialAdmin();assertThat(state.getInitialPasswordCiphertext()).isEqualTo(ciphertext);assertThatThrownBy(()->service.revealInitialAdminPassword("wrong-secret" )).isInstanceOf(ApiException.class);}
}

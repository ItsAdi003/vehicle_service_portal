package com.vsbp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Role;
import com.vsbp.repository.AppUserRepository;
import com.vsbp.service.BusinessException;
import com.vsbp.service.UserService;
import com.vsbp.web.form.RegistrationForm;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository users;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(users, encoder);
    }

    @Test
    void registerStoresHashedPasswordLowerCasedEmailAndCustomerRole() {
        when(users.existsByEmailIgnoreCase("asha@example.com")).thenReturn(false);
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = service.register(form("  Asha Rao ", "Asha@Example.com", "Secret123"));

        assertThat(saved.getEmail()).isEqualTo("asha@example.com");
        assertThat(saved.getFullName()).isEqualTo("Asha Rao");
        assertThat(saved.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(saved.getPasswordHash()).isNotEqualTo("Secret123");
        assertThat(encoder.matches("Secret123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("asha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(form("Asha", "ASHA@example.com", "Secret123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already registered");
        verify(users, never()).save(any());
    }

    @Test
    void authenticateAcceptsCorrectPasswordIgnoringEmailCase() {
        AppUser user = new AppUser("Asha", "asha@example.com", encoder.encode("Secret123"), null, Role.CUSTOMER);
        when(users.findByEmailIgnoreCase("asha@example.com")).thenReturn(Optional.of(user));

        assertThat(service.authenticate("ASHA@example.com ", "Secret123")).contains(user);
    }

    @Test
    void authenticateRejectsWrongPassword() {
        AppUser user = new AppUser("Asha", "asha@example.com", encoder.encode("Secret123"), null, Role.CUSTOMER);
        when(users.findByEmailIgnoreCase("asha@example.com")).thenReturn(Optional.of(user));

        assertThat(service.authenticate("asha@example.com", "WrongPassword")).isEmpty();
    }

    @Test
    void authenticateRejectsUnknownUser() {
        when(users.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        assertThat(service.authenticate("nobody@example.com", "Secret123")).isEmpty();
    }

    private static RegistrationForm form(String name, String email, String password) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName(name);
        form.setEmail(email);
        form.setPassword(password);
        return form;
    }
}

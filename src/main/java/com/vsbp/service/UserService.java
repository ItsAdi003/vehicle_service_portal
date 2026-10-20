package com.vsbp.service;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Role;
import com.vsbp.repository.AppUserRepository;
import com.vsbp.web.form.RegistrationForm;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegistrationForm form) {
        String email = normalise(form.getEmail());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("This e-mail is already registered");
        }
        String phone = form.getPhone() == null || form.getPhone().isBlank() ? null : form.getPhone().trim();
        AppUser user = new AppUser(
                form.getFullName().trim(),
                email,
                passwordEncoder.encode(form.getPassword()),
                phone,
                Role.CUSTOMER);
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> authenticate(String email, String rawPassword) {
        return users.findByEmailIgnoreCase(normalise(email))
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    private static String normalise(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}

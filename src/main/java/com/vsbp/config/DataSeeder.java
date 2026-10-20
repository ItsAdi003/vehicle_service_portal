package com.vsbp.config;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Role;
import com.vsbp.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the demo admin account on first start. Safe to run on every start (idempotent). */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(AppUserRepository users,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.admin-email:admin@vsbp.local}") String adminEmail,
                      @Value("${app.seed.admin-password:Admin@123}") String adminPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmailIgnoreCase(adminEmail)) {
            users.save(new AppUser("Service Admin", adminEmail.toLowerCase(), passwordEncoder.encode(adminPassword), null, Role.ADMIN));
            log.info("Seeded admin account {}", adminEmail);
        }
    }
}

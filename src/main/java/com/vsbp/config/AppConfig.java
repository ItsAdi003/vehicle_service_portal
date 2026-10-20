package com.vsbp.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    /** Injectable clock so time-dependent rules (past slots) can be unit tested. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** Passwords are stored only as BCrypt hashes. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

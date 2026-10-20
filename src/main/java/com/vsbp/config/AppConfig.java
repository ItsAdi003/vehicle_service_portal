package com.vsbp.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Injectable clock so time-dependent rules (past slots) can be unit tested. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

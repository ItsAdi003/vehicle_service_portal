package com.vsbp.config;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Role;
import com.vsbp.domain.ServiceSlot;
import com.vsbp.repository.AppUserRepository;
import com.vsbp.repository.ServiceSlotRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the demo admin account and the slots for the next 7 days.
 * Safe to run on every start (idempotent): it only inserts what is missing.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    static final List<LocalTime> DAILY_START_TIMES =
            List.of(LocalTime.of(9, 0), LocalTime.of(11, 0), LocalTime.of(14, 0), LocalTime.of(16, 0));
    static final int DAYS_AHEAD = 7;

    private final AppUserRepository users;
    private final ServiceSlotRepository slots;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final int slotCapacity;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(AppUserRepository users,
                      ServiceSlotRepository slots,
                      PasswordEncoder passwordEncoder,
                      Clock clock,
                      @Value("${app.seed.slot-capacity:3}") int slotCapacity,
                      @Value("${app.seed.admin-email:admin@vsbp.local}") String adminEmail,
                      @Value("${app.seed.admin-password:Admin@123}") String adminPassword) {
        this.users = users;
        this.slots = slots;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.slotCapacity = slotCapacity;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedSlots();
    }

    private void seedAdmin() {
        if (!users.existsByEmailIgnoreCase(adminEmail)) {
            users.save(new AppUser("Service Admin", adminEmail.trim().toLowerCase(Locale.ROOT),
                    passwordEncoder.encode(adminPassword), null, Role.ADMIN));
            log.info("Seeded admin account {}", adminEmail);
        }
    }

    private void seedSlots() {
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        int created = 0;
        for (int day = 0; day < DAYS_AHEAD; day++) {
            LocalDate date = tomorrow.plusDays(day);
            for (LocalTime time : DAILY_START_TIMES) {
                if (!slots.existsBySlotDateAndStartTime(date, time)) {
                    slots.save(new ServiceSlot(date, time, slotCapacity));
                    created++;
                }
            }
        }
        if (created > 0) {
            log.info("Seeded {} service slots", created);
        }
    }
}

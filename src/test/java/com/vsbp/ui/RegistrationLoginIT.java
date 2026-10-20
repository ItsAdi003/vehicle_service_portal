package com.vsbp.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Journey 1: a new visitor registers, is validated, logs in and out. */
class RegistrationLoginIT extends BaseSeleniumIT {

    @Test
    void visitorRegistersLogsInAndLogsOut() {
        String email = uniqueEmail();

        register("Asha Rao", email, PASSWORD);
        waitForPath("/login");
        assertThat(text("flash-success")).contains("Registration successful");

        login(email, PASSWORD);
        waitForPath("/slots");
        assertThat(text("current-user")).isEqualTo("Asha Rao");
        assertThat(text("page-title")).isEqualTo("Available service slots");

        logout();
        assertThat(text("flash-success")).contains("logged out");

        open("/slots");
        waitForPath("/login");
        assertThat(text("page-title")).isEqualTo("Log in");
    }

    @Test
    void registrationRejectsWeakPasswordAndDuplicateEmail() {
        String email = uniqueEmail();

        register("Asha Rao", email, "short");
        assertThat(text("password-error")).contains("Password must be 8 to 72 characters");

        register("Asha Rao", email, PASSWORD);
        waitForPath("/login");

        register("Someone Else", email, PASSWORD);
        assertThat(text("email-error")).contains("already registered");
    }

    @Test
    void loginWithWrongPasswordShowsError() {
        login(ADMIN_EMAIL, "definitely-wrong");

        assertThat(text("flash-error")).isEqualTo("Invalid email or password");
        assertThat(text("page-title")).isEqualTo("Log in");
    }
}

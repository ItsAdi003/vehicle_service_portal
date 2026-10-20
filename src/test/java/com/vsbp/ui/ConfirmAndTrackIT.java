package com.vsbp.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/** Journey 4: the workshop confirms and completes a booking; the customer tracks the status. */
class ConfirmAndTrackIT extends BaseSeleniumIT {

    @Test
    void adminConfirmsAndCompletesAndCustomerTracksStatus() {
        String customer = registerAndLogin();
        requestBooking(firstBookableSlotId(), "MH12AB9999");
        String bookingId = newestBookingId();
        logout();

        loginAsAdmin();
        assertStatus("admin-bookings-table", bookingId, "REQUESTED");
        click("confirm-btn-" + bookingId);
        waitForPath("/admin/bookings");
        assertThat(text("flash-success")).contains("confirmed");
        assertStatus("admin-bookings-table", bookingId, "CONFIRMED");
        click("complete-btn-" + bookingId);
        waitForPath("/admin/bookings");
        assertStatus("admin-bookings-table", bookingId, "COMPLETED");
        logout();

        login(customer, PASSWORD);
        waitForPath("/slots");
        open("/bookings/" + bookingId);
        assertThat(text("booking-status")).isEqualTo("COMPLETED");
        assertThat(driver.findElements(By.id("timeline-requested"))).hasSize(1);
        assertThat(driver.findElements(By.id("timeline-confirmed"))).hasSize(1);
        assertThat(driver.findElements(By.id("timeline-completed"))).hasSize(1);
        assertThat(driver.findElements(By.id("timeline-cancelled"))).isEmpty();
    }

    @Test
    void customerCannotOpenAdminPage() {
        registerAndLogin();

        open("/admin/bookings");

        assertThat(text("error-status")).isEqualTo("403");
    }
}

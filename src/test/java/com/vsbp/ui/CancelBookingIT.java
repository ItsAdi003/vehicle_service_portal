package com.vsbp.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/** Journey 5: a customer cancels a booking and the seat becomes available again. */
class CancelBookingIT extends BaseSeleniumIT {

    @Test
    void cancellingReleasesTheSeat() {
        registerAndLogin();
        String slotId = firstBookableSlotId();
        int seatsBefore = seatsLeft(slotId);

        requestBooking(slotId, "TN09XY4321");
        String bookingId = newestBookingId();
        assertThat(seatsLeft(slotId)).isEqualTo(seatsBefore - 1);

        open("/bookings");
        click("cancel-btn-" + bookingId);
        waitForPath("/bookings");

        assertThat(text("flash-success")).contains("cancelled");
        assertStatus("bookings-table", bookingId, "CANCELLED");
        assertThat(driver.findElements(By.id("cancel-btn-" + bookingId))).isEmpty();
        assertThat(seatsLeft(slotId)).isEqualTo(seatsBefore);
    }
}

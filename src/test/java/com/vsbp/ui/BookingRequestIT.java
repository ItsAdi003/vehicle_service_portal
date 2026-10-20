package com.vsbp.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** Journey 3: a customer requests a booking. */
class BookingRequestIT extends BaseSeleniumIT {

    @Test
    void customerRequestsBookingFromSlotList() {
        registerAndLogin();
        open("/slots");
        waitForPath("/slots");

        driver.findElement(By.cssSelector("#slots-table a.book-link")).click();
        waitForPath("/bookings/new");
        assertThat(text("slot-summary")).startsWith("Slot:");

        fillBookingForm("ka01ab1234");
        click("submit-booking");
        waitForPath("/bookings");

        assertThat(text("flash-success")).contains("requested");
        WebElement row = driver.findElement(By.cssSelector("#bookings-table tr.booking-row"));
        assertThat(row.findElement(By.cssSelector(".vehicle-number")).getText()).isEqualTo("KA01AB1234");
        assertThat(row.findElement(By.cssSelector(".booking-status")).getText()).isEqualTo("REQUESTED");
    }

    @Test
    void bookingFormValidatesRequiredFields() {
        registerAndLogin();
        open("/bookings/new?slotId=" + firstBookableSlotId());
        waitForPath("/bookings/new");

        click("submit-booking");

        assertThat(text("vehicleNumber-error")).isEqualTo("Vehicle number is required");
        assertThat(text("vehicleModel-error")).isEqualTo("Vehicle model is required");
        assertThat(text("serviceType-error")).isEqualTo("Please choose a service type");
    }
}

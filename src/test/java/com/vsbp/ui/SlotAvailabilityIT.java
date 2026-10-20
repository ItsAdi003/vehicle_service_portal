package com.vsbp.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** Journey 2: a logged-in customer looks at the open slots. */
class SlotAvailabilityIT extends BaseSeleniumIT {

    @Test
    void customerSeesUpcomingSlotsWithSeatsLeft() {
        registerAndLogin();

        assertThat(text("page-title")).isEqualTo("Available service slots");
        List<WebElement> rows = driver.findElements(By.cssSelector("#slots-table tr.slot-row"));
        assertThat(rows).hasSizeGreaterThanOrEqualTo(28); // 7 days x 4 slots

        for (WebElement row : rows) {
            String seats = row.findElement(By.cssSelector(".seats-left")).getText().trim();
            assertThat(seats).matches("\\d+|Full");
        }
        assertThat(driver.findElement(By.id("nav-bookings")).getText()).isEqualTo("My bookings");
    }

    @Test
    void guestCannotSeeSlots() {
        open("/slots");

        waitForPath("/login");
        assertThat(text("page-title")).isEqualTo("Log in");
    }
}

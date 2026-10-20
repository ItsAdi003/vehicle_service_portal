package com.vsbp;

import static org.assertj.core.api.Assertions.assertThat;

import com.vsbp.domain.ServiceSlot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ServiceSlotTest {

    private final ServiceSlot slot = new ServiceSlot(LocalDate.of(2026, 10, 22), LocalTime.of(9, 0), 3);

    @Test
    void startCombinesDateAndTime() {
        assertThat(slot.getStart()).isEqualTo(LocalDateTime.of(2026, 10, 22, 9, 0));
    }

    @Test
    void labelsAreHumanReadable() {
        assertThat(slot.getDateLabel()).isEqualTo("Thu, 22 Oct 2026");
        assertThat(slot.getTimeLabel()).isEqualTo("09:00");
    }

    @Test
    void capacityIsKept() {
        assertThat(slot.getCapacity()).isEqualTo(3);
    }
}

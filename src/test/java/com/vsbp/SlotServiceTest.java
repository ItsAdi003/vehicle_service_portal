package com.vsbp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.vsbp.domain.ServiceSlot;
import com.vsbp.repository.BookingRepository;
import com.vsbp.repository.ServiceSlotRepository;
import com.vsbp.service.BusinessException;
import com.vsbp.service.SlotAvailability;
import com.vsbp.service.SlotService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-21T04:30:00Z"), ZONE); // 21 Oct 2026, 10:00 IST

    @Mock
    private ServiceSlotRepository slots;

    @Mock
    private BookingRepository bookings;

    private SlotService service;

    @BeforeEach
    void setUp() {
        service = new SlotService(slots, bookings, clock);
    }

    private static ServiceSlot slot(long id, int capacity) {
        ServiceSlot slot = new ServiceSlot(LocalDate.of(2026, 10, 22), LocalTime.of(9, 0), capacity);
        ReflectionTestUtils.setField(slot, "id", id);
        return slot;
    }

    @Test
    void availableSeatsIsCapacityMinusActiveBookings() {
        ServiceSlot slot = slot(10L, 3);
        when(bookings.countBySlotIdAndStatusNot(eq(10L), any())).thenReturn(2L);

        assertThat(service.availableSeats(slot)).isEqualTo(1);
    }

    @Test
    void availableSeatsIsNeverNegative() {
        ServiceSlot slot = slot(10L, 2);
        when(bookings.countBySlotIdAndStatusNot(eq(10L), any())).thenReturn(5L);

        assertThat(service.availableSeats(slot)).isZero();
    }

    @Test
    void upcomingSlotsReportFullSlots() {
        ServiceSlot full = slot(1L, 1);
        ServiceSlot open = slot(2L, 3);
        when(slots.findBySlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(LocalDate.of(2026, 10, 21)))
                .thenReturn(List.of(full, open));
        when(bookings.countBySlotIdAndStatusNot(eq(1L), any())).thenReturn(1L);
        when(bookings.countBySlotIdAndStatusNot(eq(2L), any())).thenReturn(0L);

        List<SlotAvailability> result = service.upcomingSlots();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).isFull()).isTrue();
        assertThat(result.get(1).getAvailable()).isEqualTo(3);
    }

    @Test
    void availabilityOfUnknownSlotIsRejected() {
        when(slots.findById(99L)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.availability(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not exist");
    }
}

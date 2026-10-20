package com.vsbp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Booking;
import com.vsbp.domain.BookingStatus;
import com.vsbp.domain.Role;
import com.vsbp.domain.ServiceSlot;
import com.vsbp.domain.ServiceType;
import com.vsbp.repository.AppUserRepository;
import com.vsbp.repository.BookingRepository;
import com.vsbp.repository.ServiceSlotRepository;
import com.vsbp.service.BookingService;
import com.vsbp.service.BusinessException;
import com.vsbp.service.SlotService;
import com.vsbp.web.form.BookingForm;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    // 21 Oct 2026, 10:00 in Asia/Kolkata
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-21T04:30:00Z"), ZoneId.of("Asia/Kolkata"));

    @Mock
    private BookingRepository bookings;
    @Mock
    private ServiceSlotRepository slots;
    @Mock
    private AppUserRepository users;
    @Mock
    private SlotService slotService;

    private BookingService service;
    private AppUser asha;
    private ServiceSlot tomorrowSlot;

    @BeforeEach
    void setUp() {
        service = new BookingService(bookings, slots, users, slotService, clock);
        asha = user(1L, "asha@example.com", Role.CUSTOMER);
        tomorrowSlot = slot(10L, LocalDate.of(2026, 10, 22));
    }

    private static AppUser user(long id, String email, Role role) {
        AppUser user = new AppUser("Test " + id, email, "hash", null, role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static ServiceSlot slot(long id, LocalDate date) {
        ServiceSlot slot = new ServiceSlot(date, LocalTime.of(9, 0), 3);
        ReflectionTestUtils.setField(slot, "id", id);
        return slot;
    }

    private static BookingForm form(long slotId) {
        BookingForm form = new BookingForm();
        form.setSlotId(slotId);
        form.setVehicleNumber("  mh12ab1234 ");
        form.setVehicleModel(" Honda City ");
        form.setServiceType(ServiceType.OIL_CHANGE);
        form.setNotes("   ");
        return form;
    }

    private Booking existingBooking(AppUser owner) {
        return new Booking(owner, tomorrowSlot, "MH12AB1234", "Honda City", ServiceType.OIL_CHANGE, null,
                java.time.LocalDateTime.of(2026, 10, 21, 9, 0));
    }

    @Test
    void requestCreatesRequestedBookingWithNormalisedFields() {
        when(users.findById(1L)).thenReturn(Optional.of(asha));
        when(slots.findForUpdate(10L)).thenReturn(Optional.of(tomorrowSlot));
        when(slotService.availableSeats(tomorrowSlot)).thenReturn(2);
        when(bookings.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.request(1L, form(10L));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(booking.getVehicleNumber()).isEqualTo("MH12AB1234");
        assertThat(booking.getVehicleModel()).isEqualTo("Honda City");
        assertThat(booking.getNotes()).isNull();
        assertThat(booking.getUser()).isSameAs(asha);
    }

    @Test
    void requestRejectsFullSlot() {
        when(users.findById(1L)).thenReturn(Optional.of(asha));
        when(slots.findForUpdate(10L)).thenReturn(Optional.of(tomorrowSlot));
        when(slotService.availableSeats(tomorrowSlot)).thenReturn(0);

        assertThatThrownBy(() -> service.request(1L, form(10L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fully booked");
        verify(bookings, never()).save(any());
    }

    @Test
    void requestRejectsSlotInThePast() {
        ServiceSlot pastSlot = slot(11L, LocalDate.of(2026, 10, 20));
        when(users.findById(1L)).thenReturn(Optional.of(asha));
        when(slots.findForUpdate(11L)).thenReturn(Optional.of(pastSlot));

        assertThatThrownBy(() -> service.request(1L, form(11L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("in the past");
        verifyNoInteractions(slotService);
    }

    @Test
    void requestRejectsUnknownSlot() {
        when(users.findById(1L)).thenReturn(Optional.of(asha));
        when(slots.findForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.request(1L, form(99L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void ownerCanCancelOwnBooking() {
        Booking booking = existingBooking(asha);
        when(bookings.findById(5L)).thenReturn(Optional.of(booking));

        service.cancel(5L, 1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelledAt()).isNotNull();
    }

    @Test
    void otherCustomerCannotCancelBooking() {
        Booking booking = existingBooking(asha);
        when(bookings.findById(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.cancel(5L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("own bookings");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REQUESTED);
    }

    @Test
    void adminConfirmsThenCompletes() {
        Booking booking = existingBooking(asha);
        when(bookings.findById(5L)).thenReturn(Optional.of(booking));

        service.confirm(5L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        service.complete(5L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void completeBeforeConfirmIsRejected() {
        Booking booking = existingBooking(asha);
        when(bookings.findById(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.complete(5L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void customerCannotViewSomeoneElsesBookingButAdminCan() {
        Booking booking = existingBooking(asha);
        when(bookings.findById(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.getForViewer(5L, 2L, false)).isInstanceOf(BusinessException.class);
        assertThat(service.getForViewer(5L, 2L, true)).isSameAs(booking);
        assertThat(service.getForViewer(5L, 1L, false)).isSameAs(booking);
    }

    @Test
    void unknownBookingIsReported() {
        when(bookings.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }
}

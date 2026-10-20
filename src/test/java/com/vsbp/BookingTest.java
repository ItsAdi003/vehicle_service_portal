package com.vsbp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Booking;
import com.vsbp.domain.BookingStatus;
import com.vsbp.domain.Role;
import com.vsbp.domain.ServiceSlot;
import com.vsbp.domain.ServiceType;
import com.vsbp.service.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 21, 10, 0);

    private Booking newBooking() {
        AppUser user = new AppUser("Asha Rao", "asha@example.com", "hash", null, Role.CUSTOMER);
        ServiceSlot slot = new ServiceSlot(LocalDate.of(2026, 10, 22), LocalTime.of(9, 0), 3);
        return new Booking(user, slot, "MH12AB1234", "Honda City", ServiceType.OIL_CHANGE, null, T0);
    }

    @Test
    void newBookingIsRequestedAndStampedWithRequestTime() {
        Booking booking = newBooking();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(booking.getRequestedAt()).isEqualTo(T0);
        assertThat(booking.getConfirmedAt()).isNull();
        assertThat(booking.isConfirmable()).isTrue();
        assertThat(booking.isCancellable()).isTrue();
        assertThat(booking.isCompletable()).isFalse();
    }

    @Test
    void confirmMovesRequestedToConfirmed() {
        Booking booking = newBooking();

        booking.confirm(T0.plusHours(1));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getConfirmedAt()).isEqualTo(T0.plusHours(1));
        assertThat(booking.isCompletable()).isTrue();
    }

    @Test
    void completeMovesConfirmedToCompleted() {
        Booking booking = newBooking();
        booking.confirm(T0.plusHours(1));

        booking.complete(T0.plusHours(5));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(booking.getCompletedAt()).isEqualTo(T0.plusHours(5));
        assertThat(booking.isCancellable()).isFalse();
    }

    @Test
    void cannotCompleteARequestedBooking() {
        assertThatThrownBy(() -> newBooking().complete(T0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("confirmed");
    }

    @Test
    void cannotConfirmTwice() {
        Booking booking = newBooking();
        booking.confirm(T0);

        assertThatThrownBy(() -> booking.confirm(T0))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void requestedBookingCanBeCancelled() {
        Booking booking = newBooking();

        booking.cancel(T0.plusMinutes(30));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelledAt()).isEqualTo(T0.plusMinutes(30));
    }

    @Test
    void confirmedBookingCanBeCancelled() {
        Booking booking = newBooking();
        booking.confirm(T0);

        booking.cancel(T0.plusMinutes(30));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void completedBookingCannotBeCancelled() {
        Booking booking = newBooking();
        booking.confirm(T0);
        booking.complete(T0);

        assertThatThrownBy(() -> booking.cancel(T0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void cancelledBookingCannotBeCancelledOrConfirmedAgain() {
        Booking booking = newBooking();
        booking.cancel(T0);

        assertThatThrownBy(() -> booking.cancel(T0)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> booking.confirm(T0)).isInstanceOf(BusinessException.class);
    }

    @Test
    void timestampLabelsAreFormattedAndNullWhenNotReached() {
        Booking booking = newBooking();

        assertThat(booking.getRequestedAtLabel()).isEqualTo("21 Oct 2026, 10:00");
        assertThat(booking.getConfirmedAtLabel()).isNull();
    }
}

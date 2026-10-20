package com.vsbp.service;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Booking;
import com.vsbp.domain.ServiceSlot;
import com.vsbp.repository.AppUserRepository;
import com.vsbp.repository.BookingRepository;
import com.vsbp.repository.ServiceSlotRepository;
import com.vsbp.web.form.BookingForm;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final ServiceSlotRepository slots;
    private final AppUserRepository users;
    private final SlotService slotService;
    private final Clock clock;

    public BookingService(BookingRepository bookings, ServiceSlotRepository slots, AppUserRepository users,
                          SlotService slotService, Clock clock) {
        this.bookings = bookings;
        this.slots = slots;
        this.users = users;
        this.slotService = slotService;
        this.clock = clock;
    }

    /** Creates a REQUESTED booking if the slot exists, is in the future and still has a free seat. */
    @Transactional
    public Booking request(Long userId, BookingForm form) {
        AppUser user = users.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        ServiceSlot slot = slots.findForUpdate(form.getSlotId())
                .orElseThrow(() -> new BusinessException("The selected slot does not exist"));

        LocalDateTime now = LocalDateTime.now(clock);
        if (!slot.getStart().isAfter(now)) {
            throw new BusinessException("This slot is in the past");
        }
        if (slotService.availableSeats(slot) <= 0) {
            throw new BusinessException("This slot is fully booked");
        }

        String notes = form.getNotes() == null || form.getNotes().isBlank() ? null : form.getNotes().trim();
        Booking booking = new Booking(user, slot,
                form.getVehicleNumber().trim().toUpperCase(Locale.ROOT),
                form.getVehicleModel().trim(),
                form.getServiceType(), notes, now);
        return bookings.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> forUser(Long userId) {
        return bookings.findByUserIdOrderByIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Booking> all() {
        return bookings.findAllByOrderByIdDesc();
    }

    /** A customer may view only own bookings; an admin may view all. */
    @Transactional(readOnly = true)
    public Booking getForViewer(Long bookingId, Long viewerId, boolean admin) {
        Booking booking = find(bookingId);
        if (!admin && !booking.getUser().getId().equals(viewerId)) {
            throw new BusinessException("You can only view your own bookings");
        }
        return booking;
    }

    @Transactional
    public Booking cancel(Long bookingId, Long userId) {
        Booking booking = find(bookingId);
        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only cancel your own bookings");
        }
        booking.cancel(LocalDateTime.now(clock));
        return booking;
    }

    @Transactional
    public Booking confirm(Long bookingId) {
        Booking booking = find(bookingId);
        booking.confirm(LocalDateTime.now(clock));
        return booking;
    }

    @Transactional
    public Booking complete(Long bookingId) {
        Booking booking = find(bookingId);
        booking.complete(LocalDateTime.now(clock));
        return booking;
    }

    private Booking find(Long bookingId) {
        return bookings.findById(bookingId).orElseThrow(() -> new BusinessException("Booking not found"));
    }
}

package com.vsbp.service;

import com.vsbp.domain.BookingStatus;
import com.vsbp.domain.ServiceSlot;
import com.vsbp.repository.BookingRepository;
import com.vsbp.repository.ServiceSlotRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotService {

    private final ServiceSlotRepository slots;
    private final BookingRepository bookings;
    private final Clock clock;

    public SlotService(ServiceSlotRepository slots, BookingRepository bookings, Clock clock) {
        this.slots = slots;
        this.bookings = bookings;
        this.clock = clock;
    }

    /** Slots that have not started yet, ordered by date and time, each with its free seats. */
    @Transactional(readOnly = true)
    public List<SlotAvailability> upcomingSlots() {
        LocalDateTime now = LocalDateTime.now(clock);
        return slots.findBySlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(now.toLocalDate()).stream()
                .filter(slot -> slot.getStart().isAfter(now))
                .map(slot -> new SlotAvailability(slot, availableSeats(slot)))
                .toList();
    }

    @Transactional(readOnly = true)
    public SlotAvailability availability(Long slotId) {
        ServiceSlot slot = slots.findById(slotId)
                .orElseThrow(() -> new BusinessException("The selected slot does not exist"));
        return new SlotAvailability(slot, availableSeats(slot));
    }

    /** True once the slot's start time has been reached; such a slot can no longer be booked. */
    public boolean hasStarted(ServiceSlot slot) {
        return !slot.getStart().isAfter(LocalDateTime.now(clock));
    }

    /** Capacity minus bookings that still hold a seat (everything except CANCELLED). Never negative. */
    public int availableSeats(ServiceSlot slot) {
        long used = bookings.countBySlotIdAndStatusNot(slot.getId(), BookingStatus.CANCELLED);
        return (int) Math.max(0, slot.getCapacity() - used);
    }
}

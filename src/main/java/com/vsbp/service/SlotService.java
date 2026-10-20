package com.vsbp.service;

import com.vsbp.domain.ServiceSlot;
import com.vsbp.repository.ServiceSlotRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotService {

    private final ServiceSlotRepository slots;
    private final Clock clock;

    public SlotService(ServiceSlotRepository slots, Clock clock) {
        this.slots = slots;
        this.clock = clock;
    }

    /** Slots from today onwards, ordered by date and time, each with its free seats. */
    @Transactional(readOnly = true)
    public List<SlotAvailability> upcomingSlots() {
        LocalDate today = LocalDate.now(clock);
        return slots.findBySlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(today).stream()
                .map(slot -> new SlotAvailability(slot, availableSeats(slot)))
                .toList();
    }

    /** Seats still free. Bookings are introduced with the booking workflow; until then every seat is free. */
    public int availableSeats(ServiceSlot slot) {
        return slot.getCapacity();
    }
}

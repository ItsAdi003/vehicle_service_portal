package com.vsbp.service;

import com.vsbp.domain.ServiceSlot;

/** A slot together with the number of seats still free. */
public class SlotAvailability {

    private final ServiceSlot slot;
    private final int available;

    public SlotAvailability(ServiceSlot slot, int available) {
        this.slot = slot;
        this.available = available;
    }

    public ServiceSlot getSlot() {
        return slot;
    }

    public int getAvailable() {
        return available;
    }

    public boolean isFull() {
        return available <= 0;
    }
}

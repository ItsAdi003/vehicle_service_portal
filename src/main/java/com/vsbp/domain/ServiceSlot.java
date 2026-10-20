package com.vsbp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** A time window in which the workshop can serve up to {@code capacity} vehicles. */
@Entity
@Table(name = "service_slots",
        uniqueConstraints = @UniqueConstraint(name = "uk_slot_date_time", columnNames = {"slot_date", "start_time"}))
public class ServiceSlot {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_LABEL = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private int capacity;

    protected ServiceSlot() {
        // required by JPA
    }

    public ServiceSlot(LocalDate slotDate, LocalTime startTime, int capacity) {
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public LocalDateTime getStart() {
        return LocalDateTime.of(slotDate, startTime);
    }

    public String getDateLabel() {
        return slotDate.format(DATE_LABEL);
    }

    public String getTimeLabel() {
        return startTime.format(TIME_LABEL);
    }
}

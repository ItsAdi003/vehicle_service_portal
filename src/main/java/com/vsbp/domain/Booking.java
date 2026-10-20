package com.vsbp.domain;

import com.vsbp.service.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A customer's request for a service slot. The status follows
 * REQUESTED -> CONFIRMED -> COMPLETED, and REQUESTED/CONFIRMED -> CANCELLED.
 * A booking occupies one seat of its slot until it is cancelled.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private ServiceSlot slot;

    @Column(name = "vehicle_number", nullable = false, length = 15)
    private String vehicleNumber;

    @Column(name = "vehicle_model", nullable = false, length = 60)
    private String vehicleModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(length = 255)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    protected Booking() {
        // required by JPA
    }

    public Booking(AppUser user, ServiceSlot slot, String vehicleNumber, String vehicleModel,
                   ServiceType serviceType, String notes, LocalDateTime now) {
        this.user = user;
        this.slot = slot;
        this.vehicleNumber = vehicleNumber;
        this.vehicleModel = vehicleModel;
        this.serviceType = serviceType;
        this.notes = notes;
        this.status = BookingStatus.REQUESTED;
        this.requestedAt = now;
    }

    // ---- status transitions (business rules live here so they are easy to unit test) ----

    public void confirm(LocalDateTime now) {
        if (status != BookingStatus.REQUESTED) {
            throw new BusinessException("Only a requested booking can be confirmed");
        }
        status = BookingStatus.CONFIRMED;
        confirmedAt = now;
    }

    public void complete(LocalDateTime now) {
        if (status != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only a confirmed booking can be completed");
        }
        status = BookingStatus.COMPLETED;
        completedAt = now;
    }

    public void cancel(LocalDateTime now) {
        if (!isCancellable()) {
            throw new BusinessException("Only a requested or confirmed booking can be cancelled");
        }
        status = BookingStatus.CANCELLED;
        cancelledAt = now;
    }

    public boolean isConfirmable() {
        return status == BookingStatus.REQUESTED;
    }

    public boolean isCompletable() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean isCancellable() {
        return status == BookingStatus.REQUESTED || status == BookingStatus.CONFIRMED;
    }

    // ---- getters ----

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public ServiceSlot getSlot() {
        return slot;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getNotes() {
        return notes;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getRequestedAtLabel() {
        return format(requestedAt);
    }

    public String getConfirmedAtLabel() {
        return format(confirmedAt);
    }

    public String getCompletedAtLabel() {
        return format(completedAt);
    }

    public String getCancelledAtLabel() {
        return format(cancelledAt);
    }

    private static String format(LocalDateTime time) {
        return time == null ? null : time.format(TIMESTAMP);
    }
}

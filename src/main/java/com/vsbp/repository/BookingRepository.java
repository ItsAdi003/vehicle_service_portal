package com.vsbp.repository;

import com.vsbp.domain.Booking;
import com.vsbp.domain.BookingStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByIdDesc(Long userId);

    List<Booking> findAllByOrderByIdDesc();

    /** Number of bookings of a slot whose status is not the given one (used with CANCELLED to count taken seats). */
    long countBySlotIdAndStatusNot(Long slotId, BookingStatus status);
}

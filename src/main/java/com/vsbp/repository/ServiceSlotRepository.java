package com.vsbp.repository;

import com.vsbp.domain.ServiceSlot;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceSlotRepository extends JpaRepository<ServiceSlot, Long> {

    List<ServiceSlot> findBySlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(LocalDate from);

    boolean existsBySlotDateAndStartTime(LocalDate date, LocalTime time);

    /** Locks the slot row so two concurrent requests cannot both take the last seat. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServiceSlot s where s.id = :id")
    Optional<ServiceSlot> findForUpdate(@Param("id") Long id);
}

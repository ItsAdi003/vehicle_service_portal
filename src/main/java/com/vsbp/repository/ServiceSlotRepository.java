package com.vsbp.repository;

import com.vsbp.domain.ServiceSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceSlotRepository extends JpaRepository<ServiceSlot, Long> {

    List<ServiceSlot> findBySlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(LocalDate from);

    boolean existsBySlotDateAndStartTime(LocalDate date, LocalTime time);
}

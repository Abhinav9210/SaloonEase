package com.saloon.repository;

import com.saloon.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByBarberIdAndDate(Long barberId, LocalDate date);
    List<TimeSlot> findByBarberIdAndDateBetween(Long barberId, LocalDate start, LocalDate end);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.barber.id = :barberId " +
           "AND ts.date = :date AND ts.isBooked = false AND ts.isAvailable = true " +
           "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlots(@Param("barberId") Long barberId, @Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TimeSlot ts " +
           "WHERE ts.barber.id = :barberId AND ts.date = :date " +
           "AND ts.startTime < :endTime AND ts.endTime > :startTime AND ts.isBooked = true")
    boolean existsConflictingSlot(@Param("barberId") Long barberId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);
}

package com.saloon.repository;

import com.saloon.entity.Booking;
import com.saloon.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);
    Page<Booking> findBySalonId(Long salonId, Pageable pageable);
    Page<Booking> findByBarberId(Long barberId, Pageable pageable);
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.salon.id = :salonId AND b.status = :status")
    Page<Booking> findBySalonIdAndStatus(@Param("salonId") Long salonId,
                                          @Param("status") BookingStatus status, Pageable pageable);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.salon.id = :salonId " +
           "AND b.status = 'COMPLETED' AND b.bookedAt BETWEEN :from AND :to")
    Double sumEarningsBySalonAndDateRange(@Param("salonId") Long salonId,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.salon.id = :salonId " +
           "AND b.bookedAt BETWEEN :from AND :to")
    long countBookingsBySalonAndDateRange(@Param("salonId") Long salonId,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    long countByStatus(BookingStatus status);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'COMPLETED'")
    Double sumTotalRevenue();
}

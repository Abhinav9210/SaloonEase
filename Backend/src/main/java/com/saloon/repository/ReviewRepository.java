package com.saloon.repository;

import com.saloon.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findBySalonId(Long salonId, Pageable pageable);
    Page<Review> findByBarberId(Long barberId, Pageable pageable);
    List<Review> findByCustomerId(Long customerId);
    boolean existsByCustomerIdAndBookingId(Long customerId, Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.salon.id = :salonId")
    Double avgRatingBySalon(@Param("salonId") Long salonId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.barber.id = :barberId")
    Double avgRatingByBarber(@Param("barberId") Long barberId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.salon.id = :salonId")
    long countBySalon(@Param("salonId") Long salonId);
}

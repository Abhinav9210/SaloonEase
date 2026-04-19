package com.saloon.repository;

import com.saloon.entity.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    List<Barber> findBySalonId(Long salonId);
    List<Barber> findBySalonIdAndIsAvailable(Long salonId, boolean isAvailable);
    Optional<Barber> findByUserId(Long userId);

    @Query("SELECT b FROM Barber b WHERE b.salon.id = :salonId ORDER BY b.rating DESC")
    List<Barber> findBySalonIdOrderByRatingDesc(@Param("salonId") Long salonId);

    boolean existsByUserIdAndSalonId(Long userId, Long salonId);
}

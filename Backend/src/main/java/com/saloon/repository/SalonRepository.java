package com.saloon.repository;

import com.saloon.entity.Salon;
import com.saloon.entity.enums.SalonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalonRepository extends JpaRepository<Salon, Long> {
    List<Salon> findByOwnerId(Long ownerId);
    List<Salon> findByStatus(SalonStatus status);
    Page<Salon> findByStatus(SalonStatus status, Pageable pageable);

    @Query("SELECT s FROM Salon s WHERE s.status = 'APPROVED' AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Salon> searchApprovedSalons(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM Salon s WHERE s.status = 'APPROVED' AND LOWER(s.city) = LOWER(:city)")
    List<Salon> findApprovedByCity(@Param("city") String city);

    @Query("SELECT s FROM Salon s WHERE s.status = 'APPROVED' ORDER BY s.rating DESC")
    List<Salon> findTopRatedApproved(Pageable pageable);

    long countByStatus(SalonStatus status);
}

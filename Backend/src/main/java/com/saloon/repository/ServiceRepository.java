package com.saloon.repository;

import com.saloon.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findBySalonId(Long salonId);
    List<Service> findBySalonIdAndIsActive(Long salonId, boolean isActive);
}

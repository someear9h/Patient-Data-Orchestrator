package com.pm.analyticsservice.repository;

import com.pm.analyticsservice.model.PatientAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PatientAnalyticsRepository extends JpaRepository<PatientAnalytics, Long> {

    // Spring Data JPA magic: Finds the exact row based on our 4 dimensions
    Optional<PatientAnalytics> findByEventDateAndAgeGroupAndEventTypeAndEmailDomain(
            LocalDate eventDate, String ageGroup, String eventType, String emailDomain
    );
}
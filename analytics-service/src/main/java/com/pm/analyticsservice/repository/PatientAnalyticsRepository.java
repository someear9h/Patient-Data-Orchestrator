package com.pm.analyticsservice.repository;

import com.pm.analyticsservice.model.PatientAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PatientAnalyticsRepository extends JpaRepository<PatientAnalytics, Long> {
    Optional<PatientAnalytics> findByEventDateAndAgeGroup(LocalDate eventDate, String ageGroup);
}

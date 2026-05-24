package com.pm.analyticsservice.service;

import com.pm.analyticsservice.model.PatientAnalytics;
import com.pm.analyticsservice.repository.PatientAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private final PatientAnalyticsRepository repository;

    public AnalyticsService(PatientAnalyticsRepository repository) {
        this.repository = repository;
    }

    public void processPatientEvent(PatientEvent event) {
        final String ageGroup = calculateAgeGroup(event.getDateOfBirth(), event.getPatientId());
        final String domain = extractDomain(event.getEmail());
        final String eventType = extractEventType(event.getEventType());
        final LocalDate today = LocalDate.now();

        PatientAnalytics analytics = repository
                .findByEventDateAndAgeGroupAndEventTypeAndEmailDomain(today, ageGroup, eventType, domain)
                .orElseGet(() -> {
                    PatientAnalytics newRecord = new PatientAnalytics();
                    newRecord.setEventDate(today);
                    newRecord.setAgeGroup(ageGroup);
                    newRecord.setEventType(eventType);
                    newRecord.setEmailDomain(domain);
                    newRecord.setEventCount(0);
                    return newRecord;
                });

        analytics.setEventCount(analytics.getEventCount() + 1);
        repository.save(analytics);
    }

    // *** private Helper Methods ***

    private String calculateAgeGroup(String dobStr, String patientId) {
        if (dobStr == null || dobStr.trim().isEmpty()) {
            return "UNKNOWN";
        }
        try {
            LocalDate dob = LocalDate.parse(dobStr);
            int age = Period.between(dob, LocalDate.now()).getYears();
            if (age < 18) return "0-17";
            if (age < 30) return "18-29";
            if (age < 45) return "30-44";
            if (age < 60) return "45-59";
            return "60+";
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse DOB for Patient ID {}: {}", patientId, dobStr);
            return "UNKNOWN";
        }
    }

    private String extractDomain(String email) {
        if (email != null && !email.trim().isEmpty() && email.contains("@")) {
            return email.substring(email.indexOf("@") + 1).toLowerCase();
        }
        return "unknown";
    }

    private String extractEventType(String rawEventType) {
        if (rawEventType != null && !rawEventType.trim().isEmpty()) {
            return rawEventType;
        }
        return "UNKNOWN";
    }
}
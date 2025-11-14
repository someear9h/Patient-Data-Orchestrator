package com.pm.analyticsservice.service;

import com.pm.analyticsservice.model.PatientAnalytics;
import com.pm.analyticsservice.repository.PatientAnalyticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class AnalyticsService {
    private final PatientAnalyticsRepository repository;

    public AnalyticsService(PatientAnalyticsRepository repository) {
        this.repository = repository;
    }

    public void processPatientEvent(String dateOfBirthStr) {
        LocalDate dob = LocalDate.parse(dateOfBirthStr);
        int age = Period.between(dob, LocalDate.now()).getYears();

        // defining age groups
        String ageGroup;
        if (age < 18) ageGroup = "0-17";
        else if (age < 30) ageGroup = "18-29";
        else if (age < 45) ageGroup = "30-44";
        else if (age < 60) ageGroup = "45-59";
        else ageGroup = "60+";

        LocalDate today = LocalDate.now();

        PatientAnalytics analytics = repository
                .findByEventDateAndAgeGroup(today, ageGroup)
                .orElseGet(() -> {
                    PatientAnalytics newRecord = new PatientAnalytics();
                    newRecord.setEventDate(today);
                    newRecord.setAgeGroup(ageGroup);
                    newRecord.setSignupCount(0);
                    return newRecord;
                });

        analytics.setSignupCount(analytics.getSignupCount() + 1);
        repository.save(analytics);
    }
}

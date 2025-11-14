package com.pm.analyticsservice.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient_analytics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_date", "age_group"})
})
public class PatientAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(name = "signup_count", nullable = false)
    private int signupCount = 0;

    private Long getId() {
        return id;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public int getSignupCount() {
        return signupCount;
    }

    public void setSignupCount(int signupCount) {
        this.signupCount = signupCount;
    }
}

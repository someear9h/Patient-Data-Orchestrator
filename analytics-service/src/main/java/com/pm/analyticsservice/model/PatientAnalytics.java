package com.pm.analyticsservice.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient_analytics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_date", "age_group", "event_type", "email_domain"})
})
public class PatientAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "email_domain", nullable = false)
    private String emailDomain;

    @Column(name = "event_count", nullable = false)
    private int eventCount = 0; // Renamed from signupCount

    // --- Getters and Setters ---
    public Long getId() { return id; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEmailDomain() { return emailDomain; }
    public void setEmailDomain(String emailDomain) { this.emailDomain = emailDomain; }

    public int getEventCount() { return eventCount; }
    public void setEventCount(int eventCount) { this.eventCount = eventCount; }
}
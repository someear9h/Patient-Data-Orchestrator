package com.pm.analyticsservice.controller;

import com.pm.analyticsservice.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import patient.events.PatientEvent;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    // Same shape as AnalyticsRestClient.PatientEventRequest on the sender side.
    public record PatientEventRequest(
            String patientId,
            String name,
            String email,
            String eventType,
            String dateOfBirth
    ) {}

    @PostMapping("/patient-created")
    public ResponseEntity<Void> receivePatientEvent(@RequestBody PatientEventRequest req) {
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(req.patientId())
                .setName(req.name())
                .setEmail(req.email())
                .setEventType(req.eventType())
                .setDateOfBirth(req.dateOfBirth())
                .build();

        service.processPatientEvent(event);
        return ResponseEntity.ok().build();
    }
}


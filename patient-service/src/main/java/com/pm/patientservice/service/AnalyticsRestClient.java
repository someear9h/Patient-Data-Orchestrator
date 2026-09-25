package com.pm.patientservice.service;

import com.pm.patientservice.model.Patient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AnalyticsRestClient {
    private final RestClient restClient;

    public AnalyticsRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("http://analytics-service:4002")
                .build();
    }

    // Mirrors the fields in PatientEvent proto, so the receiver can rebuild it.
    public record PatientEventRequest(
            String patientId,
            String name,
            String email,
            String eventType,
            String dateOfBirth
    ) {}

    public void sendPatientCreated(Patient patient) {
        PatientEventRequest req = new PatientEventRequest(
                patient.getId().toString(),
                patient.getName(),
                patient.getEmail(),
                "PATIENT CREATED",
                patient.getDateOfBirth().toString()
        );

        restClient.post()
                .uri("/analytics/patient-created")
                .body(req)
                .retrieve()
                .toBodilessEntity();
    }
}

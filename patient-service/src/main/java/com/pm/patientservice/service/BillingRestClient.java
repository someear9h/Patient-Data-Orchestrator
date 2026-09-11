package com.pm.patientservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BillingRestClient {
    private RestClient restClient;

    public BillingRestClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://billing-service:4001")
                .build();
    }

    // the name of the fields must be same as BillingRequestDTO
    // or else JSON keys wont line up and we will get null for name and email
    record BillingRequestObject(String patientId, String name, String email) {}

    public void sendPatientCreated(BillingRequestObject request) {

        restClient.post()
                .uri("/billing/patient-created")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}

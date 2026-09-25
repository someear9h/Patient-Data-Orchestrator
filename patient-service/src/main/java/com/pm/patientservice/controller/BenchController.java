package com.pm.patientservice.controller;

import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.service.BillingRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bench")
@RequiredArgsConstructor
public class BenchController {
    private final BillingServiceGrpcClient grpc;
    private final BillingRestClient rest;
//    private final BillingGrpcBlockingStub grpcStub; // raw stub for fair comparison

    @PostMapping("/billing/{transport}")
    public ResponseEntity<Void> benchBilling(@PathVariable String transport,
                                             @RequestBody BenchRequest req) {
        long t0 = System.nanoTime();
        switch (transport) {
            case "grpc" -> grpc.createBillingAccount(req.patientId(), req.name(), req.email());
            case "rest" -> rest.sendPatientCreated(new BillingRestClient.BillingRequestObject(
                    req.patientId(), req.name(), req.email()));
            default -> throw new IllegalArgumentException("unknown transport");
        }
        long t1 = System.nanoTime();
        // Return timing in a header so k6 can extract it per request
        return ResponseEntity.ok()
                .header("X-Server-Time-Ns", Long.toString(t1 - t0))
                .build();
    }

    public record BenchRequest(String patientId, String name, String email) {}
}

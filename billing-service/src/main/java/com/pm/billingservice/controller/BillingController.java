package com.pm.billingservice.controller;

import com.pm.billingservice.dto.BillingResponseDTO;
import com.pm.billingservice.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingRepository billingRepository;

    @GetMapping("/account/{patientId}")
    public ResponseEntity<BillingResponseDTO> getBillingByPatient(@PathVariable UUID patientId) {
        return billingRepository.findByPatientId(patientId)
                .map(acc -> ResponseEntity.ok(BillingResponseDTO.builder()
                        .patientId(acc.getPatientId().toString()) // Convert UUID to String for JSON
                        .accountId(acc.getAccountId())
                        .status(acc.getStatus())
                        .balance(acc.getBalance())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }
}
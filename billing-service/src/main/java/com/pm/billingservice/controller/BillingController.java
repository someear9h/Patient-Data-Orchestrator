package com.pm.billingservice.controller;

import com.pm.billingservice.dto.BillingRequestDTO;
import com.pm.billingservice.dto.BillingResponseDTO;
import com.pm.billingservice.model.BillingAccount;
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

    @PostMapping("/patient-created")
    public ResponseEntity<BillingResponseDTO> createBillingAccount(
            @RequestBody BillingRequestDTO request) {

        UUID patientUuid = UUID.fromString(request.patientId());

        BillingAccount account = BillingAccount.builder()
                .patientId(patientUuid)
                .accountId("BILL-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8))
                .status("ACTIVE")
                .balance(0)
                .build();

        BillingAccount savedAccount = billingRepository.save(account);

        BillingResponseDTO response = BillingResponseDTO.builder()
                .patientId(savedAccount.getPatientId().toString())
                .accountId(savedAccount.getAccountId())
                .status(savedAccount.getStatus())
                .balance(savedAccount.getBalance())
                .build();

        return ResponseEntity.ok(response);
    }

}
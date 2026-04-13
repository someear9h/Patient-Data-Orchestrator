package com.pm.billingservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillingResponseDTO {
    private String patientId;
    private String accountId;
    private String status;
    private int balance;
}
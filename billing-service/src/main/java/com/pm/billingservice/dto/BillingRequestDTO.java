package com.pm.billingservice.dto;

public record BillingRequestDTO (String patientId,
                                 String name,
                                 String email){}

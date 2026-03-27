package com.auditservice.controller;

import com.auditservice.model.AuditLog;
import com.auditservice.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // This fetches all logs from Elasticsearch
    @GetMapping
    public Iterable<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
package com.auditservice.kafka;

import audit.events.AuditEvent;
import com.auditservice.model.AuditLog;
import com.auditservice.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

// This mirrors  analytics-service exactly,
// but parses AuditEvent and saves it to Elasticsearch.
@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final AuditLogRepository auditLogRepository;

    public KafkaConsumer(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @KafkaListener(topics = "system-audit", groupId = "audit-service-group")
    public void consumeEvent(byte[] event) {
        try {
            AuditEvent auditEvent = AuditEvent.parseFrom(event);

            log.info("Received Audit Event: [Action = {}, Service = {}, ResourceId = {}]",
                    auditEvent.getAction(),
                    auditEvent.getServiceName(),
                    auditEvent.getResourceId());

            // Map Protobuf object to Elasticsearch Document
            AuditLog logEntry = AuditLog.builder()
                    .eventId(auditEvent.getEventId())
                    .timestamp(auditEvent.getTimestamp())
                    .serviceName(auditEvent.getServiceName())
                    .action(auditEvent.getAction())
                    .performedBy(auditEvent.getPerformedBy())
                    .resourceId(auditEvent.getResourceId())
                    .details(auditEvent.getDetails())
                    .build();

            // Save to Elasticsearch
            auditLogRepository.save(logEntry);
            log.debug("Audit log saved successfully to Elasticsearch.");

        } catch (Exception e) {
            log.error("Error Deserializing Audit Event {}", e.getMessage());
        }
    }
}

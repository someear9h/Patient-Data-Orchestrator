package com.pm.patientservice.kafka;

import audit.events.AuditEvent;
import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.time.Instant;
import java.util.UUID;

@Service
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT CREATED")
                .setDateOfBirth(patient.getDateOfBirth().toString())
                .build();

        try {
            kafkaTemplate.send("patient", event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending PATIENT CREATED event {}", event);
        }
    }

    // method to send audit event
    public void sendAuditEvent(String action, String performedBy, String resourceId, String details) {
        AuditEvent auditEvent = AuditEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTimestamp(Instant.now().toString())
                .setServiceName("patient-service")
                .setAction(action)
                .setPerformedBy(performedBy)
                .setResourceId(resourceId)
                .setDetails(details)
                .build();

        try {
            kafkaTemplate.send("system-audit", auditEvent.toByteArray());
            log.info("Audit event sent to system-audit topic for action: {}", action);
        } catch (Exception e) {
            log.error("Error sending audit event for action: {}", action, e);
        }
    }
}

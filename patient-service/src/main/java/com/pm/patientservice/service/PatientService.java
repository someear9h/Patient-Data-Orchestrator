package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Base64;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient,
                   KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();

        // Optional: Auditing "Reads" can create a lot of logs in production,
        // but it's great for demonstrating HIPAA compliance in your project!
        String performedByEmail = extractEmailFromJwt();
        kafkaProducer.sendAuditEvent(
                "READ_ALL_PATIENTS",
                performedByEmail,
                "ALL",
                "Fetched full list of all patients."
        );

        return patients.stream()
                .map(patient -> PatientMapper.toDTO(patient)).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {

        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A Patient with this email already exists: "
                    + patientRequestDTO.getEmail());
        }

        Patient newPatient = patientRepository
                .save(PatientMapper.toModel(patientRequestDTO));

        try {
            billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),
                    newPatient.getName(), newPatient.getEmail());
        } catch (Exception e) {
            // In production, i maybe might want to log this properly instead of printStackTrace
            e.printStackTrace();
        }

        kafkaProducer.sendEvent(newPatient);

        // Fetch the email dynamically, or fallback to "system_user" if it fails
        String performedByEmail = extractEmailFromJwt();

        kafkaProducer.sendAuditEvent(
                "CREATE_PATIENT",
                performedByEmail,
                newPatient.getId().toString(),
                "Created new patient record for email: " + newPatient.getEmail()
        );

        return PatientMapper.toDTO(newPatient);
    }

    /**
     * Helper method to grab the Authorization header from the current HTTP request,
     * decode the JWT, and extract the "sub" (email) claim.
     */
    private String extractEmailFromJwt() {
        try {
            // Grab the current HTTP request from Spring's context
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");

                // Check if the header exists and contains a Bearer token
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7); // Remove "Bearer " prefix
                    String[] chunks = token.split("\\.");   // JWTs are split by dots

                    if (chunks.length > 1) {
                        // Decode the payload (the second chunk)
                        Base64.Decoder decoder = Base64.getUrlDecoder();
                        String payload = new String(decoder.decode(chunks[1]));

                        // Parse the JSON payload to get the "sub" field
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(payload);
                        if (node.has("sub")) {
                            return node.get("sub").asText(); // This is "testuser@test.com"
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract email from JWT for audit logging: " + e.getMessage());
        }

        // Fallback if there is no token (e.g., during an internal system call)
        return "system_user";
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with ID: " + id));

        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A Patient with this email already exists: "
                    + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);

        // Fire Audit Event
        String performedByEmail = extractEmailFromJwt();
        kafkaProducer.sendAuditEvent(
                "UPDATE_PATIENT",
                performedByEmail,
                updatedPatient.getId().toString(),
                "Updated patient details for email: " + updatedPatient.getEmail()
        );

        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);

        // Fire Audit Event
        String performedByEmail = extractEmailFromJwt();
        kafkaProducer.sendAuditEvent(
                "DELETE_PATIENT",
                performedByEmail,
                id.toString(),
                "Deleted patient record."
        );
    }
}
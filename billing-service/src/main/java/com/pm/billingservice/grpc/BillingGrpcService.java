package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.repository.BillingRepository;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);
    private final BillingRepository billingRepository;

    @Override
    public void createBillingAccount(BillingRequest billingrequest,
                                     StreamObserver<BillingResponse> responseObserver) {

        // 1. Convert String from gRPC request to Java UUID
        UUID patientUuid = UUID.fromString(billingrequest.getPatientId());

        // 2. Build the Entity
        BillingAccount savedAccount = getOrCreate(patientUuid);

        log.info("Billing account created in DB for patient: {}", patientUuid);

        // 4. Build gRPC Response using the SAVED data
        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId(savedAccount.getAccountId())
                .setStatus(savedAccount.getStatus())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private BillingAccount getOrCreate(UUID patientUuid) {
        return billingRepository.findByPatientId(patientUuid).orElseGet(() -> {
            try {
                BillingAccount saved = billingRepository.save(
                        BillingAccount.builder()
                                .patientId(patientUuid)
                                .accountId("BILL-" + UUID.randomUUID().toString().substring(0, 8))
                                .status("ACTIVE")
                                .balance(0)
                                .build());
                log.info("Billing account created for patient {}", patientUuid);
                return saved;
            } catch (DataIntegrityViolationException e) {
                // Race: another thread/transport inserted between our find and save.
                log.warn("Concurrent insert for patient {}, re-reading", patientUuid);
                return billingRepository.findByPatientId(patientUuid)
                        .orElseThrow(() -> e);
            }
        });
    }
}
